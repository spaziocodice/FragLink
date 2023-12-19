package com.spaziocodice.labs.fraglink.service.impl;

import com.spaziocodice.labs.fraglink.domain.TriplePatternResponse;

public interface TriplePatternResolver extends LinkedDataFragmentResolver<TriplePatternResponse> {
    LinkedDataFragmentResolver<TriplePatternResponse> NO_OP_RESOLVER = (subject, predicate, object, graph, pageNumber) -> TriplePatternResponse.EMPTY;
}
