package co.edu.atlas.common.config;

import co.edu.atlas.auth.jwt.JwtService;
import co.edu.atlas.auth.security.JwtAuthenticationFilter;
import co.edu.atlas.auth.security.RestSecurityErrorHandler;
import co.edu.atlas.common.ratelimit.RateLimitFilter;
import co.edu.atlas.common.ratelimit.RateLimiter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLICOS = {
        "/auth/**", "/actuator/health", "/actuator/health/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
    };

    private static final long HSTS_SEGUNDOS = 31_536_000L;
    private static final String PERMISSIONS_POLICY = "camera=(), microphone=(), geolocation=()";
    private static final String CSP = "Content-Security-Policy";
    /** API JSON pura: nada se carga ni se incrusta. Swagger UI (solo dev) queda fuera porque necesita scripts. */
    private static final String CSP_API = "default-src 'none'; frame-ancestors 'none'";

    private static final RequestMatcher DOCUMENTACION = new OrRequestMatcher(
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/swagger-ui.html"),
            new AntPathRequestMatcher("/v3/api-docs/**"));

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http, JwtService jwt, RestSecurityErrorHandler errores, RateLimiter limiter)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .headers(h -> h.contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(f -> f.deny())
                        .referrerPolicy(r -> r.policy(ReferrerPolicy.NO_REFERRER))
                        .httpStrictTransportSecurity(
                                hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(HSTS_SEGUNDOS))
                        .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", PERMISSIONS_POLICY))
                        .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(
                                new NegatedRequestMatcher(DOCUMENTACION), new StaticHeadersWriter(CSP, CSP_API))))
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(errores).accessDeniedHandler(errores))
                .authorizeHttpRequests(a -> a.requestMatchers("/auth/me")
                        .authenticated()
                        .requestMatchers(PUBLICOS)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(new RateLimitFilter(limiter, errores), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwt), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** La autenticacion es por JWT; se declara para evitar el usuario en memoria con clave generada. */
    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Autenticacion solo por JWT");
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(props.allowedOrigins());
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
