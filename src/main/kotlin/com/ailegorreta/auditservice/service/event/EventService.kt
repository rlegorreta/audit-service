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
 *  EventService.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.service.event

import com.ailegorreta.auditservice.config.ServiceConfig
import com.ailegorreta.auditservice.domain.Event
import com.ailegorreta.auditservice.domain.EventRepository
import com.ailegorreta.auditservice.gql.types.Notification
import com.ailegorreta.commons.event.EventDTO
import com.ailegorreta.commons.event.EventType
import com.ailegorreta.commons.utils.HasLogger
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import reactor.core.publisher.Sinks
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.collections.LinkedHashMap

/**
 * EventService that stores the events int the mongo DB
 *
 *  @author rlh
 *  @project : audit-server-repo
 *  @date September 2023
 */
@Service
class EventService(private val eventRepository: EventRepository,
                   private val serviceConfig: ServiceConfig,
                   private val notificationSink: Sinks.Many<Notification>): HasLogger {

    var latch = CountDownLatch(1)

    /**
     * This function store an event based on ConfigApplication if this exists, in case that there is not exist a ConfigApplication
     * by default only store into a database the event.
     *
     * If we have a ConfigApplication that allow to store into a file we take the path and the application name to create a new file,
     * the file name will be the <ApplicationName>.txt
     */
    /* note: The @KafkaListener annotation must be uncommented just for the Kafka test (i.e., KakfkaTests.kt class)
     *       without the use os Spring cloud stream configuration
     */
    // @KafkaListener(topics = ["audit"], groupId = "group-ailegorreta")
    fun processEvent(eventDTO: EventDTO): EventDTO {
        logger.warn("Will process event $eventDTO")
        var event: Event = Event.createEventByEventDTO(eventDTO)

        when (event.eventType) {
            EventType.FULL_STORE -> {
                event = eventRepository.save(event)
                writeToFile(event)
            }
            EventType.DB_STORE -> {
                eventRepository.save(event)
            }
            EventType.FILE_STORE -> {
                event = createEventOnlyFile(event)
                writeToFile(event)
            }
            EventType.ERROR_EVENT -> {
                eventRepository.save(event)
                logger.error("En ERROR event has been received:mvn  $eventDTO")
                // writeToFile(event) // TODO define where error events must go
            }
            else -> createEventOnlyFile(event)
        }
        latch.countDown()           // just for testing purpose

        return eventDTO
    }

    fun resetLatch() {
        latch = CountDownLatch(1)
    }

    /**
     * This function create the events that are not stored into a database, add an ID that is a UUID and the current date
     *
     * @param event Is the event without an ID
     */
    private fun createEventOnlyFile(event: Event): Event {
        event.id = UUID.randomUUID()

        return event
    }

    /**
     * Write the event to a new File, the File name is based on the filepath of the configApplication
     * and the ApplicationName
     *
     * @param event The event to write in the File
     * @param filePath Is the folder
     */
    private fun writeToFile(event: Event) {
        try {
            val file = File("${serviceConfig.filePath}/${event.applicationName}.txt")
            val writer = BufferedWriter(FileWriter(file, !file.createNewFile()))

            writer.append(event.toJson().trim())
            writer.newLine()
            writer.close()
        } catch (e: IOException) {
            logger.error("An error occurred writing the event to a file.")
            e.printStackTrace()
        }
    }

    /**
     * This function store the notification in the database
     * Then we use GraphQL Subscription in order to receive on-line the stored notification (from any client)
     * and sent it to the front. The Subscription is just for the user.
     *
     */
    /* note: The @KafkaListener annotation must be uncommented just for the Kafka test (i.e., KakfkaTests.kt class)
    *       without the use os Spring cloud stream configuration
    */
    // @KafkaListener(topics = ["notify"], groupId = "group-ailegorreta")
    fun processNotification(eventDTO: EventDTO): EventDTO {
        var event: Event = Event.createEventByEventDTO(eventDTO)

        event.correlationId = "Notificación"
        eventRepository.save(event)

        val notification = Notification(eventDTO.username,
                                        title = (eventDTO.eventBody as LinkedHashMap<*, *>)["notificaFacultad"] as String,
                                        message = (eventDTO.eventBody as LinkedHashMap<*, *>)["datos"] as String)

        notificationSink.tryEmitNext(notification)

        return eventDTO
    }
}
