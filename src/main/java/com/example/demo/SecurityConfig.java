package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                // AÑADIMOS TODAS LAS RUTAS DE SWAGGER AQUÍ:
                .requestMatchers(
                    "/", "/login", "/registro", "/css/**", 
                    "/swagger-ui/**", 
                    "/v3/api-docs/**", 
                    "/swagger-ui.html", 
                    "/webjars/**",
                    "/api/**"
                ).permitAll() 
                
                // Todo lo demás protegido
                .anyRequest().authenticated() 
            )
            .formLogin(login -> login
                .loginPage("/") 
                .loginProcessingUrl("/login") 
                .defaultSuccessUrl("/dashboard", true) 
                .failureUrl("/?error=true") 
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true) 
                .deleteCookies("JSESSIONID", "ultimoUsuarioId") 
                .permitAll()
            );

        return http.build();
    }

    // Para que encripte las contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}