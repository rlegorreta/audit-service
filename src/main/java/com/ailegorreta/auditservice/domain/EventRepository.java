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
 *  EventRepository.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.domain;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Repository of the events stored on mongo database
 *
 * Whe use Spring-GraphQL auto-registration instead of declaring the DataFetchers in the
 * RuntimeWiringConfigurer. See class PostsRuntimeWiring
 *
 * This repository use QueryDsl library to be more simplistic than use a @mapping
 * annotations
 *
 * @author rlh
 * @project : audit-service
 * @date September 2023
 */
// @GraphQlRepository
public interface EventRepository extends CrudRepository<Event, UUID>,
                                         PagingAndSortingRepository<Event, UUID>,
                                         QuerydslPredicateExecutor<Event>,
                                         QuerydslBinderCustomizer<QEvent> {

    @Override
    default void customize(QuerydslBindings bindings, QEvent root) {

        // In this example we customize the QueryDsl to make contains
        bindings.bind(String.class)
                .first((StringPath path, String value) -> {
                    if (value.startsWith("%") && value.endsWith("%"))
                        return path.containsIgnoreCase(value.substring(1, value.length() - 1));
                    else
                        return path.eq(value);
        });
        // The second customization is to do range dates in eventDate attribute
        bindings.bind(root.eventDate)
                .all((path, value) -> {
                    List<? extends LocalDateTime> dates = new ArrayList<>(value);

                    if (dates.size() == 1) {
                        return Optional.of(path.eq(dates.get(0)));
                    } else {
                        LocalDateTime from = dates.get(0);
                        LocalDateTime to = dates.get(1);

                        return Optional.of(path.between(from, to));
                    }
                });
        /* Other examples that can be used for gt, le, le
        bindings.bind(root.salary)
                .first(NumberExpression::goe);
        */
    }

    /**
     * Example to do queries inside the JSON data for events. This examples reads where the JSON data
     * has telefono attribute inside.
     */
    @Query("'eventBody.datos.idUsuario': ?0 'eventBody.datos.telefono': {$regex: ?1}")
    List<Event> findEventsDetail(Integer idUsuario, String telefono);

    @Override
    Page<Event> findAll(Pageable pageable);

    @Override
    Page<Event> findAll(Predicate predicate, Pageable pageable);

    @Override
    long count(Predicate predicate);

    @Override
    List<Event> findAll(Predicate predicate);

}
