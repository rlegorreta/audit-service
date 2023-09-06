/* Copyright (c) 2023, LegoSoft Soluciones, S.C.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are not permitted.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *  ResourceServerConfig.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

/**
 * Resource server configuration.
 *
 * Many scopes are defined for this resource:
 *  - sys.facultad  audit from the system
 *  - acme.facultad audit from the acme micro services
 *  - iam.facultad audit from the iam
 *  - cartera.read audit from cartera microservice
 *
 * @author rlh
 * @project : audit-service
 * @date September 2023
 */
@EnableWebSecurity
@EnableMethodSecurity
@Configuration(proxyBeanMethods = false)
class ResourceServerConfig {
    // @formatter:off
    @Bean
    @Throws(Exception::class)
    fun securityFilterChain( http:HttpSecurity): SecurityFilterChain {
        /**
         *  -- This code is we want for develop purpose to use all REST calls without a token --
         *  -- For example: if want to run the REST from swagger and test the microservice
         * http.authorizeHttpRequests{ auth ->  auth
         *     .requestMatchers("/ **").permitAll()
         *     .anyRequest().authenticated()
         *
         * note: erse white space between '/ **' ) just for comment
         *
         **/

        http.authorizeHttpRequests{ auth ->  auth
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/audit/**").hasAnyAuthority("SCOPE_sys.facultad",
                                                                    "SCOPE_acme.facultad",
                                                                    "SCOPE_iam.facultad",
                                                                    "SCOPE_cartera.read")
                    .requestMatchers("/nosecurity/**").permitAll()
            }
            .oauth2ResourceServer{ server -> server.jwt { Customizer.withDefaults<Any>() }}
            .sessionManagement{ sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // ^ Each request must include an Access Token, so there’s no need to keep a user session alive between
            // requests. We want it to be stateless.
            .csrf { config -> config.disable() }
        // ^ Since the authentication strategy is stateless and does not involve a browser-based client, we can safely
        // disable the CSRF protection
        return http.build()
    }
    // @formatter:on
}
