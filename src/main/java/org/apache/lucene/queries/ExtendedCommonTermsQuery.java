/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.lucene.queries;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.elasticsearch.common.lucene.search.Queries;
import org.elasticsearch.index.mapper.FieldMapper;

import java.io.IOException;

/**
 * Extended version of {@link CommonTermsQuery} that allows to pass in a
 * <tt>minimumNumberShouldMatch</tt> specification that uses the actual num of high frequent terms
 * to calculate the minimum matching terms.
 */
public class ExtendedCommonTermsQuery extends CommonTermsQuery {

    private final FieldMapper<?> mapper;

    public ExtendedCommonTermsQuery(Occur highFreqOccur, Occur lowFreqOccur, float maxTermFrequency, boolean disableCoord, FieldMapper<?> mapper) {
        super(highFreqOccur, lowFreqOccur, maxTermFrequency, disableCoord);
        this.mapper = mapper;
    }

    private String lowFreqMinNumShouldMatchSpec;
    private String highFreqMinNumShouldMatchSpec;

    @Override
    protected int calcLowFreqMinimumNumberShouldMatch(int numOptional) {
        return calcMinimumNumberShouldMatch(lowFreqMinNumShouldMatchSpec, numOptional);
    }

    protected int calcMinimumNumberShouldMatch(String spec, int numOptional) {
        if (spec == null) {
            return 0;
        }
        return Queries.calculateMinShouldMatch(numOptional, spec);
    }

    @Override
    protected int calcHighFreqMinimumNumberShouldMatch(int numOptional) {
        return calcMinimumNumberShouldMatch(highFreqMinNumShouldMatchSpec, numOptional);
    }

    public void setHighFreqMinimumNumberShouldMatch(String spec) {
        this.highFreqMinNumShouldMatchSpec = spec;
    }

    public String getHighFreqMinimumNumberShouldMatchSpec() {
        return highFreqMinNumShouldMatchSpec;
    }

    public void setLowFreqMinimumNumberShouldMatch(String spec) {
        this.lowFreqMinNumShouldMatchSpec = spec;
    }

    public String getLowFreqMinimumNumberShouldMatchSpec() {
        return lowFreqMinNumShouldMatchSpec;
    }

    @Override
    protected Query newTermQuery(Term term, TermContext context) {
        if (mapper == null) {
            return super.newTermQuery(term, context);
        }
        final Query query = mapper.queryStringTermQuery(term);
        if (query == null) {
            return super.newTermQuery(term, context);
        } else {
            return query;
        }
    }
}
