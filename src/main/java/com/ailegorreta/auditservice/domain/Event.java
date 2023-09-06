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
 *  Event.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.domain;

import com.ailegorreta.auditservice.ApplicationContextProvider;
import com.ailegorreta.commons.event.EventDTO;
import com.ailegorreta.commons.event.EventType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.annotations.QueryEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * This is the document that store the events into a MongoDB
 *
 * note: This class must be a java class in order that QueryDSL maven plugin can generate
 *       correctly the QEvent class. Do not change this class to a kotlin class
 *
 * @author rlh
 * @project : audit-service
 * @date September 2023
 */
@QueryEntity
@Document
public class Event {
    private UUID        id;
    private Integer     token;
    private String      correlationId;
    private EventType   eventType;
    private String      username;
    private String      eventName;
    private LocalDateTime   eventDate;
    private String applicationName;
    private Object      eventBody;

    public static Event createEventByEventDTO(EventDTO eventDTO) {
        return new Event(UUID.randomUUID(), 0, eventDTO.getCorrelationId(), eventDTO.getEventType(),
                         eventDTO.getUsername(), eventDTO.getEventName(), LocalDateTime.now(),
                         eventDTO.getApplicationName(), eventDTO.getEventBody().toString()
                    );

    }

    public Event(UUID id, Integer token, String correlationId,  EventType eventType,
                 String username, String eventName, LocalDateTime eventDate,
                 String applicationName, Object eventBody) {
        this.id = id;   this.token = token; this.correlationId = correlationId; this.eventType = eventType;
        this.username = username; this.eventName = eventName; this.eventDate = eventDate;
        this.applicationName = applicationName; this.eventBody = eventBody;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getToken() {
        return token;
    }

    public void setToken(Integer token) {
        this.token = token;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Object getEventBody() {
        return eventBody;
    }

    public void setEventBody(Object eventBody) {
        this.eventBody = eventBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;

        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Return the Event as a JSON format
     */
    public String toJson() {
        try {
            return ApplicationContextProvider.getBean(ObjectMapper.class).writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
