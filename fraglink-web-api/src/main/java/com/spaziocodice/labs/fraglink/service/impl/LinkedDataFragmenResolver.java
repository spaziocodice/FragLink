package com.spaziocodice.labs.fraglink.service.impl;

import com.spaziocodice.labs.fraglink.domain.LinkedDataFragmentResponse;

import java.util.Optional;

@FunctionalInterface
public interface LinkedDataFragmenResolver<O extends LinkedDataFragmentResponse<?>> {
    O linkedDataFragment(Optional<String> subject, Optional<String> predicate, Optional<String> object, Optional<String> graph);
}
