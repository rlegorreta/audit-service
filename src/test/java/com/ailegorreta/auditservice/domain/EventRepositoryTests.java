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
 *  EventRepositoryTests.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.domain;

import com.ailegorreta.auditservice.EnableTestContainers;
import com.ailegorreta.commons.event.EventDTO;
import com.ailegorreta.commons.event.EventType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
/* ^ This is just the case we wanted to test just he MongoDB Repositories and not download all context */
@EnableTestContainers
@ActiveProfiles("integration-tests")
public class EventRepositoryTests {

    @MockBean
    private StreamBridge streamBridge;
    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    EventRepository eventRepository;

    @Test
    void givenEvent_thenSaveItAndFindAll() {
        var eventDTO = new EventDTO(
                "correlationId",
                EventType.DB_STORE,
                "test",
                "EVENT_TEST",
                "AUDIT",
                "CORE TEST",
                """
                        {
                           "body": "test"
                        }
                        """);
        var event = Event.createEventByEventDTO(eventDTO);

        eventRepository.save(event);
        assertThat(((Collection<Event>)eventRepository.findAll()).size()).isEqualTo(1);
    }

    /**
     * Test to do queries inside the JSON data for events. This examples reads where the JSON data
     * has telephone attribute inside.
     *
     * Query("'eventBody.datos.idUsuario' : ?0 'eventBody.datos.telefono' : { $regex: ?1}")
     */
    @Test
    void givenEvent_thenSaveItAndFindUsingEventBodyInternalData() {
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
                           "nombre" : "Juan",
                           "datos": {
                                "idUsuario": 123,
                                "telefono": "5591495040",
                                "direccion": "Prado Sur 240 2do piso"
                           }
                        }
                        """);
        var event = Event.createEventByEventDTO(eventDTO);

        eventRepository.save(event);
        assertThat(eventRepository.findEventsDetail(123, "5591495040").size()).isEqualTo(1);
    }

}
