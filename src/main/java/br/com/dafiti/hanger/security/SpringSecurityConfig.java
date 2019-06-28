/*
 * Copyright (c) 2018 Dafiti Group
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package br.com.dafiti.hanger.security;

import br.com.dafiti.hanger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 *
 * @author Valdiney V GOMES
 */
@Configuration
@EnableWebSecurity
@EnableScheduling
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userDetailService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    
    @Autowired
    private @Value("${hanger.anonymous.access:true}") boolean anonymousEnabled;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers(
                        "/observer",
                        "/webjars/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/customization/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //Identify if anonymous access is enabled.
        if (anonymousEnabled) {
            http
                    .authorizeRequests()
                    .antMatchers("/",
                            "/home",
                            "/**/list",
                            "/**/view/**",
                            "/**/detail/**",
                            "/**/search/**",
                            "/**/log/**",
                            "/flow/**",
                            "/propagation/**",
                            "/**/user/confirmation/**",
                            "/**/alter/",
                            "/error/**",
                            "/user/edit/**").permitAll();
        }

        http
                .authorizeRequests()
                .antMatchers(
                        "/user/add/",
                        "/user/role/**",
                        "/user/active/**",
                        "/configuration/**").access("hasRole('HERO')")
                .antMatchers(
                        "/**/delete/**",
                        "/**/rebuild/**").access("hasRole('ADMIN') || hasRole('HERO')")
                .antMatchers(
                        "/**/edit/**",
                        "/**/add/**").access("hasRole('USER') || hasRole('ADMIN') || hasRole('HERO')")
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage("/login").permitAll().defaultSuccessUrl("/home").successHandler(loginSuccessHandler())
                .and()
                .logout().logoutUrl("/logout").permitAll().logoutSuccessUrl("/home")
                .and()
                .requestCache()
                .and()
                .exceptionHandling().accessDeniedPage("/403");

        http
                .sessionManagement()
                .maximumSessions(1)
                .sessionRegistry(sessionRegistry());
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService)
                .passwordEncoder(bCryptPasswordEncoder);
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
    }
}
