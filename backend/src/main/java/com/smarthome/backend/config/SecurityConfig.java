package com.smarthome.backend.config;

import com.smarthome.backend.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomUserDetailsService userDetailsService;

        @Autowired
        private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                // Public
                                                .requestMatchers("/login", "/login-2fa", "/css/**", "/js/**", "/error",
                                                                "/api/auth/**", "/api/recovery/**", "/forgot-password")
                                                .permitAll()

                                                // Admin Only
                                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                                // Technician Only
                                                .requestMatchers("/technician/**", "/api/technician/**")
                                                .hasRole("TECHNICIAN")

                                                // Usage & Energy: Admin or Homeowner Only
                                                .requestMatchers("/usage", "/api/energy/**")
                                                .hasAnyRole("ADMIN", "HOMEOWNER")

                                                // Device Management
                                                .requestMatchers(HttpMethod.POST, "/api/devices/**")
                                                .hasAnyRole("ADMIN", "HOMEOWNER")
                                                .requestMatchers(HttpMethod.PUT, "/api/devices/**")
                                                .hasAnyRole("ADMIN", "HOMEOWNER")
                                                .requestMatchers(HttpMethod.DELETE, "/api/devices/**")
                                                .hasAnyRole("ADMIN", "HOMEOWNER")

                                                // Device Reading
                                                .requestMatchers(HttpMethod.GET, "/api/devices/**").authenticated()

                                                // General Pages
                                                .requestMatchers("/home/**").authenticated()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .successHandler(customAuthenticationSuccessHandler)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .authenticationProvider(authenticationProvider());

                return http.build();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(userDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
