package com.hcmus.ecommerce_backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.hcmus.ecommerce_backend.security.CustomAuthenticationEntryPoint;
import com.hcmus.ecommerce_backend.security.filter.CustomBearerTokenAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            final CustomBearerTokenAuthenticationFilter customBearerTokenAuthenticationFilter,
            final CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
        http
                .exceptionHandling(customizer -> customizer.authenticationEntryPoint(customAuthenticationEntryPoint))
                .cors(customizer -> customizer.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Authentication endpoints
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()

                        // User registration
                        .requestMatchers(HttpMethod.POST, "/users/**").permitAll()

                        // Swagger/OpenAPI documentation
                        .requestMatchers("/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()

                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/ws/info/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/ws/**").permitAll()

                        // Public GET endpoints
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/colors/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/sizes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/blogs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/product-images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/product-color-sizes/**").permitAll()

                        // Admin endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/categories/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categories/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categories/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/products/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/products/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/blogs/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/blogs/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/blogs/**").hasAuthority("ADMIN")

                        // User-specific endpoints - require authentication
                        .requestMatchers("/orders/**").authenticated()
                        .requestMatchers("/cart-items/**").authenticated()
                        .requestMatchers("/reviews/create").authenticated()
                        .requestMatchers("/messages/**").authenticated()

                        .anyRequest().permitAll())
                .sessionManagement(auth -> auth.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(customBearerTokenAuthenticationFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry.addMapping("/**")
//                        .allowedOriginPatterns("*")
//                        .allowedMethods("*")
//                        .allowedHeaders("*")
//                        .allowCredentials(true);
//            }
//        };
//    }
}