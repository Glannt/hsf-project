package com.hsf.hsfproject.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableMethodSecurity
@Slf4j
public class WebSecurityConfiguration {

    @Autowired
    private AppConfiguration applicationConfiguration;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

//    @Autowired
//    JwtFilter jwtAuthFilter;
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, 
                                              HttpServletResponse response, 
                                              Authentication authentication) throws IOException, ServletException {
                log.info("Authentication successful for user: {}", authentication.getName());
                log.info("User authorities: {}", authentication.getAuthorities());
                
                // Redirect based on user role
                if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                    log.info("Redirecting ADMIN to /admin");
                    response.sendRedirect("/admin");
                } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
                    log.info("Redirecting MANAGER to /manager/products");
                    response.sendRedirect("/manager/products");
                } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
                    log.info("Redirecting USER to /products");
                    response.sendRedirect("/products");
                } else {
                    log.info("No specific role found, redirecting to /");
                    response.sendRedirect("/");
                }
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(configure -> configure
                        // Public endpoints
                        .requestMatchers("/",
                                "/products",
                                "/products/**",
                                "/product/**",
                                "/assets/**",
                                "/css/**",
                                "/js/**",
                                "/login",
                                "/register",
                                "/api/auth/**").permitAll()
                        
                        // API endpoints with role-based access
                        .requestMatchers("/api/orders/my-orders").hasAuthority("ROLE_USER")
                        .requestMatchers("/api/orders").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
                        .requestMatchers("/api/orders/*/status").hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        
                        // Traditional MVC endpoints
                        .requestMatchers("/user/**").hasAuthority("ROLE_USER")
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/manager/**").hasAuthority("ROLE_MANAGER")
                        .requestMatchers("/cart/**").hasAuthority("ROLE_USER")
                        .requestMatchers("/orders/**").hasAuthority("ROLE_USER")
                        
                        .anyRequest().authenticated())
                        
                // Form-based login for web UI
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                
                // JWT configuration for API endpoints
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authenticationProvider(applicationConfiguration.authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        ;
        return http.build();
    }
    



}
