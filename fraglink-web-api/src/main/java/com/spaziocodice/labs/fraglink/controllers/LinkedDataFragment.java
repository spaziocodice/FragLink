package com.spaziocodice.labs.fraglink.controllers;

import com.spaziocodice.labs.fraglink.domain.LinkedDataFragmentResponse;
import com.spaziocodice.labs.fraglink.log.MessageCatalog;
import com.spaziocodice.labs.fraglink.service.impl.LinkedDataFragmentResolver;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VOID;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.spaziocodice.labs.fraglink.Identifiers.FIRST_PAGE;
import static com.spaziocodice.labs.fraglink.Identifiers.GRAPH_PARAMETER_NAME;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_COLLECTION;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_MAPPING;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_MEMBER;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_PAGED_COLLECTION;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_PARTIAL_COLLECTION;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_PROPERTY;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_SEARCH;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_TEMPLATE;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_VARIABLE;
import static com.spaziocodice.labs.fraglink.Identifiers.LAST_PAGE;
import static com.spaziocodice.labs.fraglink.Identifiers.MAX_STATEMENTS_IN_PAGE;
import static com.spaziocodice.labs.fraglink.Identifiers.NEXT_PAGE;
import static com.spaziocodice.labs.fraglink.Identifiers.OBJECT_PARAMETER_NAME;
import static com.spaziocodice.labs.fraglink.Identifiers.PAGE_NUMBER_PARAMETER_NAME;
import static com.spaziocodice.labs.fraglink.Identifiers.PREDICATE_PARAMETER_NAME;
import static com.spaziocodice.labs.fraglink.Identifiers.PREVIOUS_PAGE;
import static com.spaziocodice.labs.fraglink.Identifiers.QUAD_PATTERN_RESOLVER;
import static com.spaziocodice.labs.fraglink.Identifiers.SD;
import static com.spaziocodice.labs.fraglink.Identifiers.SUBJECT_PARAMETER_NAME;
import static com.spaziocodice.labs.fraglink.Identifiers.TOTAL_MATCHES;
import static com.spaziocodice.labs.fraglink.Identifiers.TRIPLE_PATTERN_RESOLVER;
import static com.spaziocodice.labs.fraglink.service.impl.TriplePatternResolver.NO_OP_RESOLVER;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

@RestController
@Slf4j
@RequestMapping("/fragments")
    public class LinkedDataFragment {

    @Value("${fraglink.page.maxStatements:50}")
    private int maxStatementsInPage;

    @Value("${fraglink.base.url}")
    private String baseUrl;

    @Value("${fraglink.dataset.name}")
    private String datasetName;

    @Value("${fraglink.dataset.description:}")
    private String datasetDescription;

    @Value("${fraglink.fragment.name:}")
    private String fragmentName;

    @Value("${fraglink.fragment.description:}")
    private String fragmentDescription;

    @Autowired
    private ApplicationContext serviceFactory;

    private String template;

    @PostConstruct
    public void init() {
        this.template = baseUrl + "{subject,predicate,object,graph,page}";
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @GetMapping("/{provenanceCode}")
    public Dataset linkedDataFragment(
            @PathVariable("provenanceCode") String provenance,
            @RequestParam(name = SUBJECT_PARAMETER_NAME, required = false) String subject,
            @RequestParam(name = PREDICATE_PARAMETER_NAME, required = false) String predicate,
            @RequestParam(name = OBJECT_PARAMETER_NAME, required = false) String object,
            @RequestParam(name = GRAPH_PARAMETER_NAME, required = false) String graph,
            @RequestParam(name = PAGE_NUMBER_PARAMETER_NAME, required = false) Integer pageNumber,
            HttpServletRequest request) {
        return null;
    }

    private String fragmentIRI(HttpServletRequest request) {
        var iri = baseUrl +
                    request.getRequestURI() +
                    Stream.of(ofNullable(request.getParameter(SUBJECT_PARAMETER_NAME)).map(v -> SUBJECT_PARAMETER_NAME + "=" + v).orElse(null),
                                ofNullable(request.getParameter(PREDICATE_PARAMETER_NAME)).map(v -> PREDICATE_PARAMETER_NAME + "=" + v).orElse(null),
                                ofNullable(request.getParameter(OBJECT_PARAMETER_NAME)).map(v -> OBJECT_PARAMETER_NAME + "=" + v).orElse(null),
                                ofNullable(request.getParameter(OBJECT_PARAMETER_NAME)).map(v -> OBJECT_PARAMETER_NAME + "=" + v).orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("&", "?", ""));
        return iri.endsWith("?") ? iri.substring(0, iri.length() - 1) : iri;
    }

    @GetMapping()
    public Dataset linkedDataFragment(
            @RequestParam(name = SUBJECT_PARAMETER_NAME, required = false) String subject,
            @RequestParam(name = PREDICATE_PARAMETER_NAME, required = false) String predicate,
            @RequestParam(name = OBJECT_PARAMETER_NAME, required = false) String object,
            @RequestParam(name = GRAPH_PARAMETER_NAME, required = false) String graph,
            @RequestParam(name = PAGE_NUMBER_PARAMETER_NAME, required = false) Integer pageNumber,
            HttpServletRequest request) {
        var model = ModelFactory.createDefaultModel();

        var dataset = model.createResource(baseUrl + "#dataset");
        var metadataUri = baseUrl + "#metadata";
        var fragment = model.createResource(fragmentIRI(request));
        var fragmentURL = baseUrl + request.getRequestURI() + ofNullable(request.getQueryString()).map(value -> "?" + value).orElse("");

        var response = resolver(false).linkedDataFragment(
                                                            ofNullable(subject),
                                                            ofNullable(predicate),
                                                            ofNullable(object),
                                                            ofNullable(graph),
                                                            ofNullable(pageNumber));

        // fragmentIRI void:triples 129
        fragment.addLiteral(VOID.triples,
                               response.getFragmentCardinality()
                                       .map(model::createTypedLiteral)
                                       .orElseGet( () -> model.createTypedLiteral(0L)))
                .addProperty(RDF.type, HYDRA_COLLECTION)
                .addProperty(RDF.type, HYDRA_PARTIAL_COLLECTION)
                .addProperty(RDF.type, HYDRA_PAGED_COLLECTION)
                .addProperty(MAX_STATEMENTS_IN_PAGE, model.createTypedLiteral(maxStatementsInPage));

        dataset.addProperty(RDF.type, VOID.Dataset)
               .addProperty(VOID.subset, fragment);

        var templateAndMapping = model.createResource()
                .addProperty(HYDRA_TEMPLATE, template)
                .addProperty(HYDRA_MAPPING,
                        model.createResource()
                                .addProperty(HYDRA_VARIABLE, SUBJECT_PARAMETER_NAME)
                                .addProperty(HYDRA_PROPERTY, RDF.subject))
                .addProperty(HYDRA_MAPPING,
                        model.createResource()
                                .addProperty(HYDRA_VARIABLE, PREDICATE_PARAMETER_NAME)
                                .addProperty(HYDRA_PROPERTY, RDF.predicate))
                .addProperty(HYDRA_MAPPING,
                        model.createResource()
                                .addProperty(HYDRA_VARIABLE, OBJECT_PARAMETER_NAME)
                                .addProperty(HYDRA_PROPERTY, RDF.object))
                .addProperty(HYDRA_MAPPING,
                        model.createResource()
                                .addProperty(HYDRA_VARIABLE, OBJECT_PARAMETER_NAME)
                                .addProperty(HYDRA_PROPERTY, SD));

        dataset.addProperty(HYDRA_SEARCH, templateAndMapping);

        withControls(model, response, dataset, fragment, ofNullable(pageNumber));

        return DatasetFactory.create()
                    .setDefaultModel(response.patternSolution())
                    .addNamedModel(metadataUri, model);
    }

    private Model withMetadata(
            Model model,
            LinkedDataFragmentResponse<?> response,
            String datasetUri,  // #dataset
            String metadataUri, // #metadata
            String fragmentUri) { // uri corrente /en
        model.createResource(metadataUri).addProperty(FOAF.primaryTopic, model.createResource(fragmentUri));

        var dataset = model.createResource(datasetUri)
                            .addProperty(HYDRA_MEMBER, model.createProperty(datasetUri))
                            .addProperty(RDF.type, VOID.Dataset)
                            .addProperty(DCTerms.title, datasetName)
                            .addProperty(DCTerms.source, "<" + datasetUri + ">")
                            .addProperty(RDF.type, HYDRA_COLLECTION )
                            .addProperty(VOID.subset, model.createProperty(baseUrl));

        ofNullable(datasetDescription).filter(not(String::isBlank)).ifPresent(description -> dataset.addProperty(DCTerms.description, description));

        response.getDatasetCardinality().stream()
                                        .map(model::createTypedLiteral)
                                        .findFirst()
                                        .ifPresent(count -> dataset.addLiteral(TOTAL_MATCHES, count)
                                                                   .addLiteral(VOID.triples, count));
        var fragment = model.createResource(fragmentUri)
                            .addProperty(RDF.type, HYDRA_COLLECTION)
                            .addProperty(RDF.type, HYDRA_PARTIAL_COLLECTION)
                            .addProperty(RDF.type, HYDRA_PAGED_COLLECTION)
                            .addProperty(VOID.subset, datasetUri)
                            .addProperty(DCTerms.source, "<" + datasetUri + ">")
                            .addProperty(MAX_STATEMENTS_IN_PAGE, model.createTypedLiteral(maxStatementsInPage));

        fragment.addProperty(DCTerms.title, ofNullable(fragmentName).filter(not(String::isBlank)).orElseGet( () -> datasetName + ": Linked Data Fragment"));
        ofNullable(fragmentDescription).filter(not(String::isBlank)).ifPresent(description -> fragment.addProperty(DCTerms.description, description));

        response.getFragmentCardinality().stream()
                                         .map(model::createTypedLiteral)
                                         .findFirst()
                                         .ifPresent(count -> fragment.addLiteral(TOTAL_MATCHES, count)
                                                                     .addLiteral(VOID.triples, count));
        return model;
    }

    public Model withControls(Model model,
                              LinkedDataFragmentResponse<?> response,
                              Resource dataset,
                              Resource fragment,
                              Optional<Integer> pageNumber) {
        var fragmentUrl = url(fragment.getURI());
        var firstPageUri = model.createResource(fragmentUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, "1").toString());
        fragment.addProperty(FIRST_PAGE, firstPageUri);

        final int currentPageNumber = pageNumber.orElse(1);
        if (currentPageNumber > 1) {
            var prevPageNumber = Long.toString(currentPageNumber - 1);
            var prevPageId = model.createResource(fragmentUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, prevPageNumber).toString());
            fragment.addProperty(PREVIOUS_PAGE, prevPageId);
        }

        if (response.getFragmentCardinality().isPresent()) {
            var lastPageNumber = (response.getFragmentCardinality().get() / maxStatementsInPage) + 1;
            var nextPageId = model.createResource(fragmentUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, Long.toString(currentPageNumber + 1)).toString());
            fragment.addProperty(NEXT_PAGE, nextPageId);
            if (currentPageNumber != lastPageNumber) {
                var lastPageId = model.createResource(fragmentUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, Long.toString(lastPageNumber)).toString());
                fragment.addProperty(LAST_PAGE, lastPageId);
            }
        }

//        var triplePattern = model.createResource()
//                                .addProperty(HYDRA_TEMPLATE, template)
//                                .addProperty(HYDRA_MAPPING,
//                                              model.createResource()
//                                                   .addProperty(HYDRA_VARIABLE, SUBJECT_PARAMETER_NAME)
//                                                   .addProperty(HYDRA_PROPERTY, RDF.subject))
//                                .addProperty(HYDRA_MAPPING,
//                                             model.createResource()
//                                                  .addProperty(HYDRA_VARIABLE, PREDICATE_PARAMETER_NAME)
//                                                  .addProperty(HYDRA_PROPERTY, RDF.predicate))
//                                .addProperty(HYDRA_MAPPING,
//                                             model.createResource()
//                                                  .addProperty(HYDRA_VARIABLE, OBJECT_PARAMETER_NAME)
//                                                  .addProperty(HYDRA_PROPERTY, RDF.object))
//                                .addProperty(HYDRA_MAPPING,
//                                    model.createResource()
//                                         .addProperty(HYDRA_VARIABLE, OBJECT_PARAMETER_NAME)
//                                         .addProperty(HYDRA_PROPERTY, SD));
//
//        model.createResource(datasetUri).addProperty(HYDRA_SEARCH, triplePattern );
        return model;
    }

    @SneakyThrows
    private URIBuilder url(String baseUrl) {
        return new URIBuilder(baseUrl);
    }

    public LinkedDataFragmentResolver<?> resolver(boolean weAreInGraphContext) {
        try {
            return serviceFactory.getBean(weAreInGraphContext ? QUAD_PATTERN_RESOLVER : TRIPLE_PATTERN_RESOLVER,
                                          LinkedDataFragmentResolver.class);
        } catch (NoSuchBeanDefinitionException exception) {
            log.warn(MessageCatalog._00003_NO_PATTERN_RESOLVER);
            return NO_OP_RESOLVER;
        }
    }
}
