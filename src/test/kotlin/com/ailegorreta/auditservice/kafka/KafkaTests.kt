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
 *  KafkaTests.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.kafka

import com.ailegorreta.auditservice.AuditserviceApplication
import com.ailegorreta.auditservice.EnableTestContainers
import com.ailegorreta.auditservice.config.EventConfig
import com.ailegorreta.auditservice.domain.Event
import com.ailegorreta.auditservice.domain.EventRepository
import com.ailegorreta.auditservice.service.EventServiceTest
import com.ailegorreta.auditservice.service.event.EventService
import com.ailegorreta.commons.event.EventDTO
import com.ailegorreta.commons.utils.HasLogger
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * This class utilize the EventServiceTests to send an event simulating that came from any microservice, in this case
 * the iam-service (or iam-ui) in order to be stored in the MongoDB.
 *
 * @project audit-service
 * @author rlh
 * @date September 2023
 */
@SpringBootTest(classes = [AuditserviceApplication::class], webEnvironment = SpringBootTest.WebEnvironment.NONE)
/* ^ SpringBootTest download 'all' App context */
@EnableTestContainers
/* ^ This is a custom annotation to load the containers */
@ExtendWith(MockitoExtension::class)
@EmbeddedKafka(bootstrapServersProperty = "spring.kafka.bootstrap-servers")
/* ^ this is because: https://blog.mimacom.com/embeddedkafka-kafka-auto-configure-springboottest-bootstrapserversproperty/ */
@ActiveProfiles("integration-tests")
@Import(EventConfig::class)
@DirtiesContext /* will make sure this context is cleaned and reset between different tests */
class KafkaTests: HasLogger {
    @MockBean
    private var jwtDecoder: JwtDecoder? = null			// Mocked the security JWT

    @Autowired
    private val producer: EventServiceTest? = null
    @Autowired
    var consumer: EventService? = null
    @Autowired
    private lateinit var eventRepository: EventRepository
    @Autowired
    var mapper: ObjectMapper? = null
    @Autowired
    var template: KafkaTemplate<String, EventDTO>? = null

    /**
     * Testing with Spring Cloud stream, i.e., used the configuration defines in the application.yml file and not
     * in the Kafka test container, so we not use the Kafka template.
     */
    @Test
    fun whenSendingAnAuditEvent_processTheEventAndStoreMongoDB() {
        var transaction = DummyTransaction(username = "Test", date = LocalDate.now().toString())
        var eventBody = mapper!!.readTree(mapper!!.writeValueAsString(transaction))

        logger.debug("Event body: $eventBody")
        producer!!.sendEvent(eventName = "AUDIT_IAM", value = eventBody)
        val messageConsumed = consumer!!.latch.await(10, TimeUnit.SECONDS)
        logger.debug("After message consumed $messageConsumed")

        assertTrue(messageConsumed)
        Assertions.assertThat((eventRepository.findAll() as Collection<Event?>).size).isEqualTo(1)
        // ^ validates that the event hase been stores
    }

    /**
     * Contrary to the previous test in this test we do not use any Spring cloud stream, but all the parameters defined
     * in the Kafka container and therefore in we use the Kafka template. This test is more direct and is used to check
     * that the Kafka is working without the spring cloud stream configuration.
     *
     * note: In order to work the @KafkaListener annotation has to be declared in the eventServiceClass.
     */
    @Test
    fun whenSendingWithoutSpringStreamEvent_processEventAndStoreMongoDB() {
        var transaction = DummyTransaction(username = "Test", date = LocalDate.now().toString())
        var eventBody = mapper!!.readTree(mapper!!.writeValueAsString(transaction))

        val sentDTO = producer!!.generateOnlyEvent(eventName = "AUDIT_IAM", value = eventBody)

        logger.debug("Will send the event using $template from kakfa")
        //  Here is we don´t kno why need to send a second event using Kafka template
        var sentRes = template!!.send("audit", "producerTest-out-0", sentDTO).get()
        logger.debug("Sent message:$sentRes")

        val messageConsumed = consumer!!.latch.await(10, TimeUnit.SECONDS)
        logger.debug("After consumer $messageConsumed")

        //  assertTrue(messageConsumed)
        Assertions.assertThat((eventRepository.findAll() as Collection<Event?>).size).isEqualTo(1)
        // ^ validates that the event hase been stores
    }

    private data class DummyTransaction(val id: UUID = UUID.randomUUID(),
                                        val name: String = "Dummy transaction",
                                        val username: String,
                                        val date: String)

}
