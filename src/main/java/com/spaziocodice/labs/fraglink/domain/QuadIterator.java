package com.spaziocodice.labs.fraglink.domain;

import lombok.Builder;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

import java.util.Iterator;

public final class QuadIterator { //implements Streamable<Quad>, Iterator<Quad> {
    private final Iterator<Quad> iterator;

    public QuadIterator(DatasetGraph graph) {
        this.iterator = graph.stream().iterator();
    }

   // @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    //@Override
    public Quad next() {
        return iterator.next();
    }

    //@Override
    public Iterator<Quad> iterator() {
        return iterator;
    }
}
