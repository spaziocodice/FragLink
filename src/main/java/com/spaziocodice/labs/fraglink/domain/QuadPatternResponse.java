package com.spaziocodice.labs.fraglink.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.jena.sparql.core.Quad;

@AllArgsConstructor
@Builder
public class QuadPatternResponse extends LinkedDataFragmentResponse<Quad> {
    @Builder
    public QuadPatternResponse(int pageNumber, long totalMatches, Quad matches) {
        super(pageNumber, totalMatches, matches);
    }
}
