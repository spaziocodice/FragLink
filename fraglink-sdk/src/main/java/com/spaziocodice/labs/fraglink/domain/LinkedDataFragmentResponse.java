package com.spaziocodice.labs.fraglink.domain;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public abstract class LinkedDataFragmentResponse<T> {
    protected int pageNumber;

    protected long totalMatches;

    protected T matches;
}
