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
 *  ControllerMvcTests.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.web;

import com.ailegorreta.auditservice.config.ResourceServerConfig;
import com.ailegorreta.auditservice.domain.Event;
import com.ailegorreta.auditservice.domain.EventRepository;
import com.ailegorreta.commons.event.EventDTO;
import com.ailegorreta.commons.event.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * note: Spring Data GraphQL does NOT support MockMvc. Instead, we used the GraphqlTest class.
 *
 * So this class is just for demo purpose in how we can use MockMvc inside a controller (imperative version).
 *
 * Sometimes to test Webmvc only is difficult for all existing dependencies in the application.
 *
 * @WebMvcTest
 *
 * @SpringBootTest annotation to load all context.
 *
 * @project: audit-service
 * @author: rlh
 * @date: August 2023
 */
@WebMvcTest(TestController.class)
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
/* ^ Disables the default behavior of relying on an embedded test database since we want to use Testcontainers */
// @EnableJpaRepositories(basePackages = "com.ailegorreta.paramservice.domain")
// @EntityScan(basePackages = "com.ailegorreta.paramservice.domain")
// @EnableTestContainers
@ExtendWith(MockitoExtension.class)
@Import({ResourceServerConfig.class, TestController.class})
@ActiveProfiles("integration-tests-mvc")            // This is to permit duplicate singleton beans
class ControllerMvcTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private JwtDecoder jwtDecoder;
    /* ^ Mocks the JwtDecoder so that the application does not try to call Spring Security Server and get the public
         keys for decoding the Access Token  */
    @MockBean
    private StreamBridge streamBridge;

    /* MockBeans for all repositories  */
    @MockBean
    private EventRepository eventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test how to read all Events using a normal REST controller
     */
    @Test
    void whenGetAllEventsAndAuthenticatedTheShouldReturn200() throws Exception{
        var eventDTO1 = new EventDTO(
                "correlationId1",
                EventType.DB_STORE,
                "test",
                "EVENT_TEST",
                "AUDIT",
                "CORE TEST",
                """
                        {
                           "notificaFacultad": "NOTIFICA_IAM",
                           "nombre" : "Juan Perez",
                           "datos": {
                                "idUsuario": 123,
                                "telefono": "5591495040",
                                "direccion": "Prado Sur 240 2do piso"
                           }
                        }
                        """);
        var event1 = Event.createEventByEventDTO(eventDTO1);
        var eventDTO = new EventDTO(
                "correlationId2",
                EventType.DB_STORE,
                "test",
                "EVENT_TEST",
                "AUDIT",
                "CORE TEST",
                """
                        {
                           "notificaFacultad": "NOTIFICA_IAM",
                           "nombre" : "Simon Hernandez",
                           "datos": {
                                "idUsuario": 1234,
                                "telefono": "5591495040",
                                "direccion": "Prado Sur 240 2do piso"
                           }
                        }
                        """);
        var event2 = Event.createEventByEventDTO(eventDTO1);

        eventRepository.save(event1);
        eventRepository.save(event2);
        when(eventRepository.findAll()).thenReturn(List.of(event1, event2));

        var res = mockMvc.perform(MockMvcRequestBuilders.post("/audit/tests/all")
                         .with(jwt().authorities(Arrays.asList(new SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                                               new SimpleGrantedAuthority("ROLE_ADMINLEGO"))))
                         .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        res.andExpect(status().isOk());
    }

    /**
     * Test how to read a body from an Event
     */
    @Test
    void whenGetSystemRateExistingAndAuthenticatedTheShouldReturn200() throws Exception{
        var eventDTO = new EventDTO(
                "correlationId",
                EventType.DB_STORE,
                "test",
                "EVENT_TEST",
                "AUDIT",
                "CORE TEST",
                """
                        {
                           "notificaFacultad": "NOTIFICA_IAM",
                           "nombre" : "Juan Perez",
                           "datos": {
                                "idUsuario": 123,
                                "telefono": "5591495040",
                                "direccion": "Prado Sur 240 2do piso"
                           }
                        }
                        """);
        var expectedEvent = Event.createEventByEventDTO(eventDTO);

        when(eventRepository.findEventsDetail(123,"5591495040"))
                            .thenReturn(List.of(expectedEvent));

        var res = mockMvc.perform(MockMvcRequestBuilders.post("/audit/tests/telefono")
                         .with(jwt().authorities(Arrays.asList(new SimpleGrantedAuthority("SCOPE_iam.facultad"),
                                                 new SimpleGrantedAuthority("ROLE_ADMINLEGO"))))
                         .content(objectMapper.writeValueAsString(
                                  new TestController.EventInput(123,"5591495040")))
                         .contentType(MediaType.APPLICATION_JSON)
                         .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print());

        res.andExpect(status().isOk());
    }

}
