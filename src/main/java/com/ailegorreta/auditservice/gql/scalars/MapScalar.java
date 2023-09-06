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
 *  MapScalar.java
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.auditservice.gql.scalars;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.*;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This GraphQL scalar type is to display the eventBody. No mutation is permitted for this scalar
 * so just retrieve an empty LinedHashMap
 *
 * @authos rlh
 * @project audit-service
 * @date September 2023
 */
public class MapScalar implements Coercing<Map, String> {

    public static GraphQLScalarType graphQLScalarType() {
        return GraphQLScalarType.newScalar()
                .name("Map")
                .description("Map type")
                .coercing(new MapScalar())
                .build();
    }

    @Override
    public String serialize(@NotNull Object dataFetcherResult,
                            @NotNull GraphQLContext graphQLContext,
                            @NotNull Locale locale) throws CoercingSerializeException {
        if (dataFetcherResult instanceof Map) {
            JSONObject jsonObject = new JSONObject(((Map) dataFetcherResult));

            return jsonObject.toString(JSONStyle.NO_COMPRESS);      // pretty print for the map object
        } else
            throw new CoercingSerializeException("Not a valid Map type:" + dataFetcherResult.getClass().getCanonicalName());
    }

    /**
     * This method is not implemented since we cannot make mutations in the eventBody y just
     * for reading purpose.
     */
    @Override
    public Map parseValue(@NotNull Object input,
                           @NotNull GraphQLContext graphQLContext,
                           @NotNull Locale locale) throws CoercingParseValueException {
        return new LinkedHashMap();
    }

    /**
     * This method is not implemented since we cannot make mutations in the eventBody y just
     * for reading purpose
     */
    @Override
    public Map parseLiteral(@NotNull Value<?> input,
                             @NotNull CoercedVariables variables,
                             @NotNull GraphQLContext graphQLContext,
                             @NotNull Locale locale) throws CoercingParseLiteralException {
        // TODO
        if (input instanceof StringValue)
            return new LinkedHashMap();

        throw new CoercingParseLiteralException("Value is not a valid Map string");
    }
}
