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
 *  EventJsonTests.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.web;

import com.ailegorreta.auditservice.domain.Event;
import com.ailegorreta.commons.event.EventDTO;
import com.ailegorreta.commons.event.EventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JsonTest for Event DTOs
 *
 * @project audit-service
 * @author rlh
 * @date September 2023
 */
@JsonTest
@ContextConfiguration(classes = EventJsonTests.class)
@ActiveProfiles("integration-tests")
public class EventJsonTests {
    @Autowired
    public JacksonTester<Event> json;

    @Test
    void testSerialize() throws Exception {
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
        var jsonContent = json.write(event);

        assertThat(jsonContent).extractingJsonPathStringValue("@.correlation_id")
                .isEqualTo(event.getCorrelationId());
        assertThat(jsonContent).extractingJsonPathStringValue("@.username")
                .isEqualTo(event.getUsername());
        assertThat(jsonContent).extractingJsonPathStringValue("@.event_name")
                .isEqualTo(event.getEventName());
    }

    @Test
    void testDeserialize() throws Exception {
        var eventDTO = new EventDTO(
                "correlationId",
                EventType.DB_STORE,
                "test",
                "EVENT_TEST",
                "AUDIT",
                "CORE TEST",
                "{}");
        var event = Event.createEventByEventDTO(eventDTO);
        var content = """
                {
                    "id": 
                    """ + "\"" + event.getId() + "\"," + """
                    "token":
                    """ + "\"" + event.getToken() + "\"," + """                   
                   "correlationId": "correlationId",
                   "eventName": "EVENT_TEST",
                   "applicationName": "AUDIT",
                   "coreName": "CORE TEST",
                   "eventDate":
                   """ + "\"" + event.getEventDate() + "\"," + """
                   "eventType":
                   """ + "\"" + event.getEventType() + "\"," + """
                  "username":
                   """ + "\"" + event.getUsername() + "\"," + """
                   "eventBody": "{}"
                }
                """;
        assertThat(json.parse(content))
                .usingRecursiveComparison()
                .isEqualTo(event);
    }

}
