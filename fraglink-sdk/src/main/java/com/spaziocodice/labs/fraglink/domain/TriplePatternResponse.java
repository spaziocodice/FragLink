package com.spaziocodice.labs.fraglink.domain;

import lombok.Builder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;

import java.util.Collection;
import java.util.Collections;

public class TriplePatternResponse extends LinkedDataFragmentResponse<Statement> {
    public final static TriplePatternResponse EMPTY = TriplePatternResponse.builder().matches(Collections.emptyList()).build();

    @Builder
    public TriplePatternResponse(Long datasetCardinality, Long fragmentCardinality, Integer pageNumber, Collection<Statement> matches) {
        super(datasetCardinality, fragmentCardinality, pageNumber, matches);
    }

    public Model patternSolution() {
        return matches.stream().findFirst().map(Statement::getModel).orElseGet(ModelFactory::createDefaultModel);
    }
}
