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
 *  EventPredicate.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.gql.querydsl;

import com.ailegorreta.data.mongo.querydsl.PredicateDate;
import com.ailegorreta.auditservice.domain.QEvent;
import com.querydsl.core.types.Predicate;

/**
 * For JPA (not MongoDB) use a generic example: https://www.baeldung.com/rest-api-search-language-spring-data-querydsl
 *
 * In this case for MongoDB the only example we have seen so far is using de Q class and no path.
 *
 * @project: author-service
 * @author: rlh
 * @date: September 2023
 */
public class EventPredicate {
    private SearchCriteria criteria;

    public EventPredicate(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    public Predicate getPredicate() {
        QEvent entityPath = QEvent.event;

        if (criteria.getKey().equals("eventName"))
            return entityPath.eventName.contains(criteria.getValue().toString());
        else if (criteria.getKey().equals("username"))
            return entityPath.username.contains(criteria.getValue().toString());
        else if (criteria.getKey().equals("eventDate"))
            return PredicateDate.getPredicateDate(entityPath.eventDate, criteria.getValue().toString());

        return null;
    }
}
