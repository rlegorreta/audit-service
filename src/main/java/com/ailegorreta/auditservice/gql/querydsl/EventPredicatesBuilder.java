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
 *  EventPredicatesBuilder.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.gql.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import java.util.*;

/**
 * This class builder is to ANDs all EventPredicates in just one MongoDB QueryDSl Predicate
 *
 * @project aut-service
 * @author rlh
 * @date September 2023
 */
public class EventPredicatesBuilder {
    private List<SearchCriteria> params;

    public EventPredicatesBuilder() {
        params = new ArrayList<>();
    }

    public EventPredicatesBuilder with(String key, String operation, Object value) {
        if (value instanceof  String && ((String) value).isEmpty())
            return this; // do nothing
        params.add(new SearchCriteria(key, operation, value));

        return this;
    }

    public Predicate build() {
        if (params.size() == 0)
            return null;

        List<Predicate> predicates = params.stream().map(param -> {
                                                                    EventPredicate predicate = new EventPredicate(param);
                                                                    return predicate.getPredicate();
                                                                })
                                                    .filter(Objects::nonNull)
                                                    .toList();

        BooleanBuilder result = new BooleanBuilder();

        for (Predicate predicate : predicates)
            result.and(predicate);

        return result.getValue();
    }

}
