package com.mediaserver.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mediaserver.security.JwtAuthenticationFilter;
import com.mediaserver.security.JwtTokenProvider;
import com.mediaserver.security.RootJwtAuthenticationFilter;
import com.mediaserver.security.RootJwtTokenProvider;
import com.mediaserver.security.oauth2.CustomStatelessAuthorizationRequestRepository;
import com.mediaserver.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.mediaserver.security.oauth2.OAuth2UserService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private OAuth2UserService oAuth2UserService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private CustomStatelessAuthorizationRequestRepository customRepo;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        return new JwtAuthenticationFilter(tokenProvider);
    }

    @Bean
    public RootJwtAuthenticationFilter rootJwtAuthenticationFilter(RootJwtTokenProvider rootTokenProvider) {
        return new RootJwtAuthenticationFilter(rootTokenProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public org.springframework.security.web.firewall.HttpFirewall allowUrlEncodedNewlineHttpFirewall() {
        return new org.springframework.security.web.firewall.DefaultHttpFirewall();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
                                          RootJwtAuthenticationFilter rootJwtAuthenticationFilter) throws Exception {
        http
            .cors(cors -> {
                CorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowCredentials(true);
                config.setAllowedOriginPatterns(Arrays.asList("*"));
                config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(Arrays.asList("*"));
                config.addExposedHeader("*");
                ((UrlBasedCorsConfigurationSource) source).registerCorsConfiguration("/**", config);
                cors.configurationSource(source);
            })
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .authorizeRequests(authz -> authz
                .antMatchers("/", "/error", "/favicon.ico", "/**/*.png", "/**/*.gif",
                             "/**/*.svg", "/**/*.jpg", "/**/*.html", "/**/*.css", "/**/*.js").permitAll()
                .antMatchers("/auth/**", "/oauth2/**",
                             "/payment/webhook", "/payment/success", "/payment/cancel",
                             "/payment/create-checkout-session",
                             "/payment/process-successful-payment",
                             "/root/auth/**",
                             "/razorpay/webhook", "/razorpay/**").permitAll()
                // allow split-screen endpoints for testing without auth (context-path is /api)
                .antMatchers("/content/split-streamByUniqueId/**").permitAll()
                .antMatchers("/content/screen/layoutMode").permitAll()
                .antMatchers("/admin/**").permitAll()
                .antMatchers("/root/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, authEx) -> {
                    res.setStatus(org.springframework.http.HttpStatus.UNAUTHORIZED.value());
                    res.setContentType("application/json");
                    res.getWriter().write("{\"error\":\"unauthorized\"}");
                })
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authEndpoint -> authEndpoint
                    .baseUri("/oauth2/authorize")
                    .authorizationRequestRepository(customRepo)
                )
                .redirectionEndpoint(redirEndpoint -> redirEndpoint.baseUri("/oauth2/callback/*"))
                .userInfoEndpoint(userEndpoint -> userEndpoint.userService(oAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler((req, res, ex) -> {
                    logger.error("OAuth2 login failure: {}", ex.getMessage(), ex);
                    try {
                        res.sendRedirect("/api/oauth2/error?message=" +
                                         ex.getMessage().replace(" ", "%20"));
                    } catch (Exception e) {
                        logger.error("Error in failure handler: {}", e.getMessage(), e);
                    }
                })
            );

        // Add filters in correct order - Root filter BEFORE regular JWT filter
        http.addFilterBefore(rootJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
