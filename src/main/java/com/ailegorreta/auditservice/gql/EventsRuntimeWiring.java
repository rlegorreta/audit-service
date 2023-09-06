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
 *  EventsRuntimeWiring.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.gql;

import com.ailegorreta.auditservice.gql.scalars.MapScalar;
import com.ailegorreta.auditservice.domain.EventRepository;
import com.ailegorreta.data.mongo.gql.directives.UpperCaseDirectiveWiring;
import com.ailegorreta.data.mongo.gql.scalars.InstantScalar;
import com.ailegorreta.data.mongo.gql.scalars.LocalDateTimeScalar;
import com.ailegorreta.data.mongo.gql.scalars.UUIDScalar;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.TypeRuntimeWiring;
import org.springframework.graphql.data.query.QuerydslDataFetcher;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.stereotype.Component;

/**
 * This class makes the configurations the schema.graphqls queries, mutations, etc in order mapp the schema with the
 * QueryDSL repositories.
 * This class was taken from the project spring-graphql.querydsl example. If needed more information visit the
 * link: https://github.com/hantsy/spring-graphql-sample
 *
 * @author: rlh
 * @project: audit-service
 * @date: September 2023
 */
@Component
public class EventsRuntimeWiring implements RuntimeWiringConfigurer {

    final EventRepository eventRepository;

    public EventsRuntimeWiring(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void configure(RuntimeWiring.Builder builder) {
        builder
                /* We do not insert this code: */
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                                .dataFetcher("events",
                                              QuerydslDataFetcher.builder(eventRepository)
                                              .many())
                                .dataFetcher("event",
                                             QuerydslDataFetcher.builder(eventRepository)
                                             .single())
                )
                /*
                Because instead we see Spring GraphQL auto.registration for QueryDSL
                */
                .scalar(UUIDScalar.graphQLScalarType())
                .scalar(LocalDateTimeScalar.graphQLScalarType())
                .scalar(InstantScalar.graphQLScalarType())
                .scalar(MapScalar.graphQLScalarType())
                .directive("uppercase", new UpperCaseDirectiveWiring())
                .build();
    }

}
