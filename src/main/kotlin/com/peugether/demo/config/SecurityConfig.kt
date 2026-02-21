package com.peugether.demo.config

import com.peugether.demo.auth.jwt.JwtAuthenticationFilter
import com.peugether.demo.common.exception.ErrorCode
import com.peugether.demo.common.response.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val objectMapper: ObjectMapper,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // 인증 불필요 엔드포인트
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/kakao").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/auth/kakao/callback").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                    // 수취인 비회원 접근
                    .requestMatchers(HttpMethod.GET, "/api/v1/recipient/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/recipient/**").permitAll()
                    // Swagger UI
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                    ).permitAll()
                    // 나머지는 인증 필요
                    .anyRequest().authenticated()
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { _, response, _ ->
                        response.status = 401
                        response.contentType = MediaType.APPLICATION_JSON_VALUE
                        response.characterEncoding = "UTF-8"
                        val body = ErrorResponse.of(ErrorCode.UNAUTHORIZED.code, ErrorCode.UNAUTHORIZED.message)
                        response.writer.write(objectMapper.writeValueAsString(body))
                    }
                    .accessDeniedHandler { _, response, _ ->
                        response.status = 403
                        response.contentType = MediaType.APPLICATION_JSON_VALUE
                        response.characterEncoding = "UTF-8"
                        val body = ErrorResponse.of("FORBIDDEN", "접근이 거부되었습니다")
                        response.writer.write(objectMapper.writeValueAsString(body))
                    }
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("*")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
