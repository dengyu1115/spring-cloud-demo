package org.nature.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.nature.common.model.Res;
import org.nature.security.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    @Qualifier("userDetailManager")
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthenticationManager authenticationManager;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        this.authenticationManager = this.authenticationManager();
        // 跨域共享
        http.cors()
                .and()
                // 跨域伪造请求限制无效
                .csrf().disable()
                .authorizeRequests()
                // 访问/data需要ADMIN角色
                .antMatchers("/**").hasRole("ADMIN")
                // 其余资源任何人都可访问
                .anyRequest().permitAll()
                .and()
                // 添加JWT登录拦截器
                .addFilter(new JWTAuthenticationFilter())
                // 添加JWT鉴权拦截器
                .addFilter(new JWTAuthorizationFilter())
                .sessionManagement()
                // 设置Session的创建策略为：Spring Security永不创建HttpSession 不使用HttpSession来获取SecurityContext
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // 异常处理
                .exceptionHandling()
                // 匿名用户访问无权限资源时的异常
                .authenticationEntryPoint(new JWTAuthenticationEntryPoint());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 注册跨域配置
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    private class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {


        @Override
        public Authentication attemptAuthentication(HttpServletRequest request,
                                                    HttpServletResponse response) throws AuthenticationException {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
            return authenticationManager.authenticate(token);
        }

        @Override
        protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                FilterChain chain, Authentication authResult) throws IOException {
            User user = (User) authResult.getPrincipal();
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
            String token = JwtTokenUtil.createToken(user.getUsername(), authorities.toString());
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            response.setHeader("token", JwtTokenUtil.TOKEN_PREFIX + token);
            response.getWriter().write(objectMapper.writeValueAsString(Res.ok("登录成功", null)));
        }

        @Override
        protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  AuthenticationException failed) throws IOException {
            String returnData;
            if (failed instanceof AccountExpiredException) {
                returnData = "账号过期";
            } else if (failed instanceof BadCredentialsException) {
                returnData = "密码错误";
            } else if (failed instanceof CredentialsExpiredException) {
                returnData = "密码过期";
            } else if (failed instanceof DisabledException) {
                returnData = "账号不可用";
            } else if (failed instanceof LockedException) {
                returnData = "账号锁定";
            } else if (failed instanceof InternalAuthenticationServiceException) {
                returnData = "用户不存在";
            } else {
                returnData = "未知异常";
            }
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(Res.err(returnData)));
        }
    }

    private class JWTAuthorizationFilter extends BasicAuthenticationFilter {

        public JWTAuthorizationFilter() {
            super(authenticationManager);
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain chain) throws IOException, ServletException {
            String tokenHeader = request.getHeader(JwtTokenUtil.TOKEN_HEADER);

            if (tokenHeader == null || !tokenHeader.startsWith(JwtTokenUtil.TOKEN_PREFIX)) {
                chain.doFilter(request, response);
                return;
            }

            SecurityContextHolder.getContext().setAuthentication(this.getAuthentication(tokenHeader));
            super.doFilterInternal(request, response, chain);
        }


        private UsernamePasswordAuthenticationToken getAuthentication(String tokenHeader) {
            String token = tokenHeader.replace(JwtTokenUtil.TOKEN_PREFIX, "");
            String username = JwtTokenUtil.getUsername(token);
            if (username == null) {
                return null;
            }
            String role = JwtTokenUtil.getUserRole(token);
            String[] roles = StringUtils.strip(role, "[]").split(", ");
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for (String s : roles) {
                authorities.add(new SimpleGrantedAuthority(s));
            }
            return new UsernamePasswordAuthenticationToken(username, null, authorities);
        }
    }

    private class JWTAuthenticationEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             AuthenticationException authException) throws IOException {
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(Res.err("您未登录，没有访问权限")));
        }
    }
}