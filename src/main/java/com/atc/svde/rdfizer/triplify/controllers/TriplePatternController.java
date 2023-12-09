package com.atc.svde.rdfizer.triplify.controllers;

import org.apache.jena.query.Dataset;
import org.apache.jena.vocabulary.RDF;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TriplePatternController {

    @GetMapping(value = "/fragments")
    public Dataset triplePattern(
                    @RequestParam(required = false) String subject,
                    @RequestParam(required = false) String predicate,
                    @RequestParam(required = false) String object,
                    @RequestParam(required = false) String graph, {


        return null;
    }
}
