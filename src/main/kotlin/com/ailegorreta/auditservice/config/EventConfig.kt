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

import com.ailegorreta.auditservice.gql.types.Notification
import com.ailegorreta.commons.event.EventDTO
import com.ailegorreta.auditservice.service.event.EventService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import reactor.core.publisher.Sinks
import reactor.util.concurrent.Queues
import java.util.function.Consumer

/**
 * Spring cloud stream kafka configuration.
 *
 * This class configure Kafka to listen to the events that come from any microservice in order to store
 * the events in a mongo db and forward it to other possible listener.
 *
 * To see an example : https://refactorfirst.com/spring-cloud-stream-with-kafka-communication.html
 *
 * @author rlh
 * @project : audit-service
 * @date September 2023
 */
@Component
@Configuration
class EventConfig {

    /**
     * This bean is for GraphQL RSocket reactor
     */
    @Bean
    fun notificationSink(): Sinks.Many<Notification> = Sinks.many()
                                .multicast()
                                .onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false)

    /**
     * This is the case when we receive the events from the IAM and the events are NOT forwarded to another
     * microservice listener. They are just stored in the mongoDB database.
     *
     * The application.yml is the following:
     *
    function:
        definition: consumerIam
    kafka:
        bindings:
            consumerIam-in-0:
                consumer:
                    configuration:
                        value.deserializer: com.lmass.audit.service.event.dto.EventDTODeSerializer
    bindings:
        consumerIam-in-0:
            destination: iam-audit
            consumer:
                use-native-decoding: true     # Enables using the custom deserializer
     */
    @Bean
    fun consumerAuditService(eventService: EventService) = Consumer {
            event: EventDTO -> eventService.processEvent(event)
        }

    /**
     * This is the case when we receive the events from the IAM and the events are forwarded to another
     * microservice listener. They are stored in the mongoDB database first and forwarded later.
     *
     * The application.yml is the following:
     *
    function:
        definition: processorIam
    kafka:
        bindings:
            processorIam-in-0:
                consumer:
                    configuration:
                        value.deserializer: com.lmass.audit.service.event.dto.EventDTODeSerializer
            processorIam-out-0:
                producer:
                    configuration:
                        value.serializer: com.lmass.audit.service.event.dto.EventDTOSerializer
    bindings:
        processorIam-in-0:
            destination: iam-audit
            consumer:
                use-native-decoding: true     # Enables using the custom deserializer
        processorIam-out-0:
            destination: iam
            producer:
                user-native-encoding: true    # Enables using the custom serializer
     */

    /**
     * This is the case when we receive the events from the Bpm and the events are NOT forwarded to another
     * microservice listener. They are just audit purpose.
     *
     * The application.yml is the following:
     *
    function:
        definition: consumerBpm
            kafka:
                bindings:
                    consumerBpm-in-0:
                consumer:
                    configuration:
                        value.serializer: com.lmass.audit.service.event.dto.EventDTOSerializer
         bindings:
            consumerBpm-in-0:
                destination: bpm-audit
            consumer:
                use-native-decoding: true     # Enables using the custom deserializer
     */
    @Bean
    fun consumerNot(eventService: EventService): Consumer<EventDTO> = Consumer {
            event: EventDTO -> eventService.processNotification(event)
    }
}
