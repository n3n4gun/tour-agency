package ru.naboyshchikovfp.tour_agency.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

@Configuration 
@EnableMethodSecurity 
public class SecurityConfig {
    @Bean 
    public PasswordEncoder  passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean 
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", "/register", "/error",
                "/css/**", "/js/**", "/images/**"
            ).permitAll()
            .requestMatchers(
                HttpMethod.GET, "/tours", "/tours/**"
            ).permitAll()
            .requestMatchers("/management/**").hasAnyRole("ADMIN", "MANAGER")
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )

        .formLogin(form -> form
            .loginPage("/login")            // Использует страницу, которую возвращает LoginController
            .loginProcessingUrl("/login")   // Spring Security принимает отправку формы через POST /login
            .failureUrl("/login?error")     // При неудачном входе перенаправляет обратно с параметром ошибки
            .successHandler(authenticationSuccessHandler())
            .permitAll()                    // Разрешает гостям доступ к адресам входа
        )

        .logout(Customizer.withDefaults())
        .exceptionHandling(exceptions -> exceptions.accessDeniedPage("/access-denied"));

        return http.build();
    }

    // Администратор попадает в управление заявками; для клиента сохраняется возврат к запрошенной странице.
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler clientHandler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        clientHandler.setDefaultTargetUrl("/");

        SimpleUrlAuthenticationSuccessHandler adminHandler =
                new SimpleUrlAuthenticationSuccessHandler("/admin/bookings");
        adminHandler.setAlwaysUseDefaultTargetUrl(true);

        return (request, response, authentication) -> {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            // Не возвращаем администратора на сохранённый клиентский адрес, например /bookings.
            if (isAdmin) {
                new HttpSessionRequestCache().removeRequest(request, response);
                adminHandler.onAuthenticationSuccess(request, response, authentication);
            } else {
                clientHandler.onAuthenticationSuccess(request, response, authentication);
            }
        };
    }
}
