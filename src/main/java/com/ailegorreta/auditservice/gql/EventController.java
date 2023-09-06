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
 *  EventController.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.gql;

import com.ailegorreta.auditservice.domain.QEvent;
import com.ailegorreta.auditservice.gql.querydsl.EventPredicatesBuilder;
import com.ailegorreta.auditservice.gql.types.EventQuery;
import com.ailegorreta.auditservice.gql.types.Notification;
import com.ailegorreta.auditservice.domain.Event;
import com.ailegorreta.auditservice.domain.EventRepository;
import com.ailegorreta.data.mongo.querydsl.PredicateDate;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.ZoneId;
import java.util.*;

/**
 * This controller is to be used with Spring-graphql annotations add on (not just the
 * QueryDsl). This is done because we want for the events to do queries based inside
 * the eventBody hash map and also inside the datos hash map.
 * For example to query the idUsuario and telefono data hash map from the events generated
 * by the iam server report when ACTUALIZA_USUARIO es generated.
 *
 * @uthor rlh
 * @project auth-service
 * @date September 2023
 */
@Controller
@Validated
@RequiredArgsConstructor
public class EventController {

    private final String NOTIFICATION = "NOTIFICACION";
    public final EventRepository eventRepository;
    public final Sinks.Many<Notification> notificationSink;

    /**
     * In this example a query is generated by the schema:
     *   eventsDetail(idUsuario: Int,
     *                telefono: String): [Event]
     *
     * This query is for introspect the event body event for idUsuario & telefono
     * Many combinations can be done, or generalize the query for a key value
     *
     */
    @QueryMapping
    public List<Event> eventsDetail(@Argument("idUsuario") Integer idUsuario,
                                    @Argument("telefono") String telefono) {
        return eventRepository.findEventsDetail(idUsuario, telefono);
    }

    @QueryMapping
    public Page<Event> eventsPageable(@Argument("eventQuery") EventQuery eventQuery) {
        Predicate query = new EventPredicatesBuilder()
                                    .with("eventName",":",eventQuery.getEventName())
                                    .with("username",":",eventQuery.getUsername())
                                    .with("eventDate",":",eventQuery.getEventDate())
                                    .build();

        if (query == null)
            return eventRepository.findAll(PageRequest.of(eventQuery.getPage(), eventQuery.getSize()));
        else
            return eventRepository.findAll(query, PageRequest.of(eventQuery.getPage(), eventQuery.getSize()));
    }

    @QueryMapping
    public Long eventsCount(@Argument("eventQuery") EventQuery eventQuery) {
        Predicate query = new EventPredicatesBuilder()
                                        .with("eventName",":",eventQuery.getEventName())
                                        .with("username",":",eventQuery.getUsername())
                                        .with("eventDate",":",eventQuery.getEventDate())
                                        .build();

        if (query == null) return eventRepository.count(); else return eventRepository.count(query);
    }

    /**
     * Notification controller methods
     *
     */
    @SubscriptionMapping
    Flux<Notification> notification() {
        return notificationSink.asFlux();
    }

    @QueryMapping
    public List<Notification> notifications(@Argument("username") String username) {
        BooleanBuilder query = new BooleanBuilder(QEvent.event.eventName.eq(NOTIFICATION))
                                .and(PredicateDate.getPredicateDate(QEvent.event.eventDate, "semana"));
                                // ^ list notifications no more than one week old
        BooleanBuilder queryUser = (new BooleanBuilder(QEvent.event.username.eq(username)))
                                    .or(new BooleanBuilder(QEvent.event.username.eq("*")));

        var res = eventRepository.findAll(query.and(queryUser));
        var notifications = new ArrayList<Notification>();

        res.forEach (event -> {
            notifications.add(new Notification(event.getUsername(),
                                    ((String) ((LinkedHashMap) event.getEventBody()).get("notificaFacultad")),
                                    ((String) ((LinkedHashMap) event.getEventBody()).get("datos")),
                                    event.getEventDate().atZone(ZoneId.systemDefault()).toInstant()));
        });

        return notifications;
    }
}