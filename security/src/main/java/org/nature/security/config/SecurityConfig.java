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
     * ????????????service
     */
    @Autowired
    @Qualifier("userDetailManager")
    private UserDetailsService userDetailsService;
    /**
     * json?????????
     */
    @Autowired
    private ObjectMapper objectMapper;
    /**
     * ????????????manager
     */
    private AuthenticationManager authenticationManager;

    /**
     * AuthenticationManager??????
     * @param auth manager
     * @throws Exception e
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // ?????????????????????????????????encoder
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }

    /**
     * HttpSecurity ??????
     * @param http HttpSecurity
     * @throws Exception e
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // AuthenticationManager?????????????????????????????????
        this.authenticationManager = this.authenticationManager();
        // ????????????
        http.cors()
                .and()
                // ??????????????????????????????
                .csrf().disable()
                .authorizeRequests()
                // ????????????????????????????????????
                .antMatchers("/**").hasRole("ADMIN")
                // ?????????????????????????????????
                .anyRequest().permitAll()
                .and()
                // ??????JWT???????????????
                .addFilter(new JWTAuthenticationFilter())
                // ??????JWT???????????????
                .addFilter(new JWTAuthorizationFilter())
                .sessionManagement()
                // ??????Session?????????????????????Spring Security????????????HttpSession ?????????HttpSession?????????SecurityContext
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // ????????????
                .exceptionHandling()
                // ?????????????????????????????????????????????
                .authenticationEntryPoint(new JWTAuthenticationEntryPoint());
    }

    /**
     * ????????????
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // ??????????????????
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }

    /**
     * jwt????????????????????????????????????????????????
     * @author nature
     * @version 1.0.0
     * @since 2021/5/25 9:16
     */
    private class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

        /**
         * ??????
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
            // ???????????????????????????token
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
            // ??????token???????????????
            return authenticationManager.authenticate(token);
        }

        /**
         * ??????????????????
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
            // ???????????????????????????jwt token
            String token = JwtTokenUtil.createToken(user.getUsername(), authorities.toString());
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            response.setHeader("token", JwtTokenUtil.TOKEN_PREFIX + token);
            // token??????header???????????????
            response.getWriter().write(objectMapper.writeValueAsString(Res.ok("????????????", null)));
        }

        /**
         * ??????????????????
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
                returnData = "????????????";
            } else if (failed instanceof BadCredentialsException) {
                returnData = "????????????";
            } else if (failed instanceof CredentialsExpiredException) {
                returnData = "????????????";
            } else if (failed instanceof DisabledException) {
                returnData = "???????????????";
            } else if (failed instanceof LockedException) {
                returnData = "????????????";
            } else if (failed instanceof InternalAuthenticationServiceException) {
                returnData = "???????????????";
            } else {
                returnData = "????????????";
            }
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(Res.err(returnData)));
        }
    }

    /**
     * jwt?????????????????????
     * @author nature
     * @version 1.0.0
     * @since 2021/5/25 10:06
     */
    private class JWTAuthorizationFilter extends BasicAuthenticationFilter {

        /**
         * ??????AuthenticationManager
         */
        public JWTAuthorizationFilter() {
            super(authenticationManager);
        }

        /**
         * ??????????????????
         * @param request  request
         * @param response response
         * @param chain    chain
         * @throws IOException      e
         * @throws ServletException e
         */
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain chain) throws IOException, ServletException {
            // ???header?????????token
            String tokenHeader = request.getHeader(JwtTokenUtil.TOKEN_HEADER);
            // ????????????token?????????????????????????????????????????????????????????????????????AuthenticationEntryPoint???????????????
            if (tokenHeader == null || !tokenHeader.startsWith(JwtTokenUtil.TOKEN_PREFIX)) {
                chain.doFilter(request, response);
                return;
            }
            // ??????token???????????????????????????????????????
            SecurityContextHolder.getContext().setAuthentication(this.getAuthentication(tokenHeader));
            super.doFilterInternal(request, response, chain);
        }

        /**
         * ??????token
         * @param tokenHeader header????????????token??????
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
     * ?????????????????????????????????
     * @author nature
     * @version 1.0.0
     * @since 2021/5/25 10:23
     */
    private class JWTAuthenticationEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             AuthenticationException authException) throws IOException {
            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(Res.err("?????????????????????????????????")));
        }
    }
}