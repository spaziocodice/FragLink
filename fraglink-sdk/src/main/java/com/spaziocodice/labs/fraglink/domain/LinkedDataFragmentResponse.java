package com.spaziocodice.labs.fraglink.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.jena.rdf.model.Model;

import java.util.Collection;
import java.util.Optional;
import java.util.OptionalLong;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public abstract class LinkedDataFragmentResponse<T> {
    protected Long datasetCardinality;
    protected Long fragmentCardinality;
    protected Integer pageNumber;
    protected Collection<T> matches;

    public Optional<Long> getDatasetCardinality() {
        return ofNullable(datasetCardinality);
    }

    public Optional<Long> getFragmentCardinality() {
        return ofNullable(fragmentCardinality);
    }

    public Optional<Integer> getPageNumber() {
        return ofNullable(pageNumber);
    }

    public abstract Model patternSolution();
}
