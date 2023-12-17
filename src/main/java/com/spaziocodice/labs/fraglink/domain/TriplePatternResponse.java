package com.spaziocodice.labs.fraglink.domain;

import lombok.Builder;
import org.apache.jena.graph.Triple;

public class TriplePatternResponse extends LinkedDataFragmentResponse<Triple> {

    @Builder
    public TriplePatternResponse(int pageNumber, long totalMatches, Triple matches) {
        super(pageNumber, totalMatches, matches);
    }
}
