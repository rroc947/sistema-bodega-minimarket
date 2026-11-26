package com.bodega.pedidos.config;

import com.bodega.pedidos.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/ventas").hasAnyAuthority("ROLE_ADMINISTRADOR", "ADMINISTRADOR", "ROLE_ENCARGADO", "ENCARGADO")
                .requestMatchers(HttpMethod.GET, "/api/ventas/*/boleta").hasAnyAuthority("ROLE_ADMINISTRADOR", "ADMINISTRADOR", "ROLE_ENCARGADO", "ENCARGADO", "ROLE_CAJERO", "CAJERO")
                .requestMatchers(HttpMethod.POST, "/api/ventas/**").hasAnyAuthority("ROLE_CAJERO", "CAJERO", "ROLE_ENCARGADO", "ENCARGADO", "ROLE_ADMINISTRADOR", "ADMINISTRADOR")
                .requestMatchers(HttpMethod.GET, "/api/productos/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyAuthority("ROLE_ADMINISTRADOR", "ADMINISTRADOR", "ROLE_ENCARGADO", "ENCARGADO")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAnyAuthority("ROLE_ADMINISTRADOR", "ADMINISTRADOR", "ROLE_ENCARGADO", "ENCARGADO")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAnyAuthority("ROLE_ADMINISTRADOR", "ADMINISTRADOR", "ROLE_ENCARGADO", "ENCARGADO")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            private final BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();

            @Override
            public String encode(CharSequence rawPassword) {
                return bCrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null) return false;
                if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$")) {
                    return bCrypt.matches(rawPassword, encodedPassword);
                }
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }
}
