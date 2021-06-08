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

/**
 * SecurityConfig
 * @author nature
 * @version 1.0.0
 * @since 2021/5/25 9:02
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 用户权限service
     */
    @Autowired
    @Qualifier("userDetailManager")
    private UserDetailsService userDetailsService;
    /**
     * json转换类
     */
    @Autowired
    private ObjectMapper objectMapper;
    /**
     * 权限验证manager
     */
    private AuthenticationManager authenticationManager;

    /**
     * AuthenticationManager配置
     * @param auth manager
     * @throws Exception e
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 指定权限管理类以及密码encoder
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

    /**
     * HttpSecurity 配置
     * @param http HttpSecurity
     * @throws Exception e
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // AuthenticationManager获取并赋值供拦截器使用
        this.authenticationManager = this.authenticationManager();
        // 跨域共享
        http.cors()
                .and()
                // 跨域伪造请求限制无效
                .csrf().disable()
                .authorizeRequests()
                // 访问指定路径需要指定角色
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

    /**
     * 跨域配置
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 注册跨域配置
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    /**
     * jwt登录验证拦截器（典型的模板模式）
     * @author nature
     * @version 1.0.0
     * @since 2021/5/25 9:16
     */
    private class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

        /**
         * 验证
         * @param request  request
         * @param response response
         * @return Authentication
         * @throws AuthenticationException e
         */
        @Override
        public Authentication attemptAuthentication(HttpServletRequest request,
                                                    HttpServletResponse response) throws AuthenticationException {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            // 根据用户名密码生成token
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
            // 校验token并返回结果
            return authenticationManager.authenticate(token);
        }

        /**
         * 验证成功处理
         * @param request    request
         * @param response   response
         * @param chain      chain
         * @param authResult authResult
         * @throws IOException e
         */
        @Override
        protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                FilterChain chain, Authentication authResult) throws IOException {
            User user = (User) authResult.getPrincipal();
            Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
            // 根据用户与权限生成jwt token
            String token = JwtTokenUtil.createToken(user.getUsername(), authorities.toString());
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            response.setHeader("token", JwtTokenUtil.TOKEN_PREFIX + token);
            // token放入header返回给前端
            response.getWriter().write(objectMapper.writeValueAsString(Res.ok("登录成功", null)));
        }

        /**
         * 验证失败处理
         * @param request  request
         * @param response response
         * @param failed   failed
         * @throws IOException e
         */
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

    /**
     * jwt授权验证拦截器
     * @author nature
     * @version 1.0.0
     * @since 2021/5/25 10:06
     */
    private class JWTAuthorizationFilter extends BasicAuthenticationFilter {

        /**
         * 指定AuthenticationManager
         */
        public JWTAuthorizationFilter() {
            super(authenticationManager);
        }

        /**
         * 全局校验处理
         * @param request  request
         * @param response response
         * @param chain    chain
         * @throws IOException      e
         * @throws ServletException e
         */
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain chain) throws IOException, ServletException {
            // 从header中获取token
            String tokenHeader = request.getHeader(JwtTokenUtil.TOKEN_HEADER);
            // 没有传入token则让请求去走其他的过滤器链（放行），实际会走到AuthenticationEntryPoint提示未登陆
            if (tokenHeader == null || !tokenHeader.startsWith(JwtTokenUtil.TOKEN_PREFIX)) {
                chain.doFilter(request, response);
                return;
            }
            // 解析token中包含的权限并进行权限校验
            SecurityContextHolder.getContext().setAuthentication(this.getAuthentication(tokenHeader));
            super.doFilterInternal(request, response, chain);
        }

        /**
         * 生成token
         * @param tokenHeader header中存放的token令牌
         * @return Authentication
         */
        private Authentication getAuthentication(String tokenHeader) {
            String token = tokenHeader.replace(JwtTokenUtil.TOKEN_PREFIX, "");
            String username = JwtTokenUtil.getUsername(token);
            if (username == null) {
                return null;
            }
            String role = JwtTokenUtil.getUserRole(token);
            String[] roles = StringUtils.strip(role, "[]").split(", ");
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            for (String s : roles) {
                authorities.add(new SimpleGrantedAuthority(s));
            }
            return new UsernamePasswordAuthenticationToken(username, null, authorities);
        }
    }

    /**
     * 权限验证不通过处理逻辑
     * @author nature
     * @version 1.0.0
     * @since 2021/5/25 10:23
     */
    private class JWTAuthenticationEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             AuthenticationException authException) throws IOException {
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(Res.err("您未登录，没有访问权限")));
        }
    }
}