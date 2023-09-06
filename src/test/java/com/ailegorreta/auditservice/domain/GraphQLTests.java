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
 *  GraphQLTests.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.domain;

import com.ailegorreta.auditservice.EnableTestContainers;
import com.ailegorreta.commons.event.EventDTO;
import com.ailegorreta.commons.event.EventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *  For GraphQL tester see:
 *  https://piotrminkowski.com/2023/01/18/an-advanced-graphql-with-spring-boot/
 *
 *  - How graphQlTester is created (imperative):
 *  WebTestClient client = MockMvcWebTestClient.bindToApplicationContext(context)
 *                 .configureClient()
 *                 .baseUrl("/graphql")
 *                 .build();
 *
 * WebGraphQlTester tester = WebGraphQlTester.builder(client).build();
 *
 * - For WebFlux:
 * WebTestClient client = WebTestClient.bindToApplicationContext(context)
 *                 .configureClient()
 *                 .baseUrl("/graphql")
 *                 .build();
 *
 *  WebGraphQlTester tester = WebGraphQlTester.builder(client).build();
 *
 * - And last against a running remote server:
 * WebTestClient client =WebTestClient.bindToServer()
 *                 .baseUrl("http://localhost:8080/graphql")
 *                 .build();
 *
 * WebGraphQlTester tester = WebGraphQlTester.builder(client).build();
 *
 * note: Use always GraphQLTester and not WebMvc because GraphQL does not support WebMvc tester.
 *
 * @project audit-service
 * @autho: rlh
 * @date: September 2023
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
/* ^ SpringBootTest download 'all' App context. We can use @DataMongoTest, both test slices work */
// @DataMongoTest
/* ^ This is just the case we wanted to test just the GraphQl and download all context. Both test slices work */
@EnableTestContainers
@ExtendWith(MockitoExtension.class)
// @EmbeddedKafka(bootstrapServersProperty = "spring.kafka.bootstrap-servers")
/* ^ this is because: https://blog.mimacom.com/embeddedkafka-kafka-auto-configure-springboottest-bootstrapserversproperty/ */
@ActiveProfiles("integration-tests")
@AutoConfigureGraphQlTester
public class GraphQLTests {

    @MockBean
    private StreamBridge streamBridge;
    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private GraphQlTester graphQlTester;
    @Autowired
    private EventRepository eventRepository;

    /**
     * Validates to read all events. Can also test pagination or with a Predicate
     */
    @Test
    void finAll() {

        var events = IntStream.rangeClosed(1,50)
                        .mapToObj(index -> {
                            var eventDTO = new EventDTO(
                                    Integer.valueOf(index).toString(),
                                    EventType.DB_STORE,
                                    "test",
                                    "EVENT_TEST",
                                    "AUDIT",
                                    "CORE TEST",
                                    Collections.singletonMap("body", "test"));
                            return addEvent(eventDTO);
                        })
                        .toList();
        /* Check all events count */
        String queryEventsCount = """
                    query allEventsCount {
                           eventsCount(eventQuery: {
                           eventName: "EVENT_TEST",
                           username: "test",
                           eventDate:
                    """ + "\"" + LocalDate.now() + "\"" + """
                           page: 0,
                           size: 0
                           })                      
                    }
                """;
        var count = graphQlTester.document(queryEventsCount)
                .execute()
                .path("data.eventsCount")
                .entity(Integer.class)
                .get();

        assertThat(count).isEqualTo(50);

        /* Now validates with a Predicate and Pageable */
        String queryAllEvents = """
                query allEvents {
                    eventsPageable(eventQuery: { 
                           eventName: "EVENT_TEST",
                           username: "test",
                           eventDate: 
                    """ + "\"" + LocalDate.now() + "\"" + """
                           page: 0,
                           size: 60
                    }) {
                        id
                        correlationId
                        eventType
                        username
                        eventName
                        eventDate
                        applicationName
                        eventBody
                    }
                }
        """.trim();
        List<Event> readEvents2 = graphQlTester.document(queryAllEvents)
                .execute()
                .path("data.eventsPageable[*]")
                .entityList(Event.class)
                .get();

        assertThat(readEvents2.size()).isEqualTo(events.size());
    }

    private Event addEvent(EventDTO eventDTO) {
        return eventRepository.save(Event.createEventByEventDTO(eventDTO));
    }
}
