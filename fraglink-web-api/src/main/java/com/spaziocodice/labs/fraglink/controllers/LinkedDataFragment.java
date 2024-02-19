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
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VOID;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.spaziocodice.labs.fraglink.Identifiers.FIRST_PAGE;
import static com.spaziocodice.labs.fraglink.Identifiers.GRAPH_PARAMETER_NAME;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_COLLECTION;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_EXPLICIT_REPRESENTATION;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_MAPPING;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_MEMBER;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_PAGED_COLLECTION;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_PARTIAL_COLLECTION;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_PROPERTY;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_SEARCH;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_TEMPLATE;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_TOTAL_ITEMS;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_VARIABLE;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_VARIABLE_REPRESENTATION;
import static com.spaziocodice.labs.fraglink.Identifiers.LAST_PAGE;
import static com.spaziocodice.labs.fraglink.Identifiers.NEXT_PAGE;
import static com.spaziocodice.labs.fraglink.Identifiers.OBJECT_PARAMETER_NAME;
import static com.spaziocodice.labs.fraglink.Identifiers.PAGE_NUMBER_PARAMETER_NAME;
import static com.spaziocodice.labs.fraglink.Identifiers.PREDICATE_PARAMETER_NAME;
import static com.spaziocodice.labs.fraglink.Identifiers.PREVIOUS_PAGE;
import static com.spaziocodice.labs.fraglink.Identifiers.QUAD_PATTERN_RESOLVER;
import static com.spaziocodice.labs.fraglink.Identifiers.SUBJECT_PARAMETER_NAME;
import static com.spaziocodice.labs.fraglink.Identifiers.TRIPLE_PATTERN_RESOLVER;
import static com.spaziocodice.labs.fraglink.service.impl.TriplePatternResolver.NO_OP_RESOLVER;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

@RestController
@Slf4j
@Lazy
public class LinkedDataFragment {

    @Value("${fraglink.page.maxStatements:50}")
    private int maxStatementsInPage;

    @Value("${fraglink.base.url}")
    private String baseUrl;

    @Value("${fraglink.dataset.name}")
    private String datasetName;

    @Value("${fraglink.dataset.description:}")
    private String datasetDescription;

    @Autowired
    private ApplicationContext serviceFactory;

    @PostConstruct
    public void init() {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @GetMapping("/fragments")
    public Dataset linkedDataFragment(
            @RequestParam(name = SUBJECT_PARAMETER_NAME, required = false) String subject,
            @RequestParam(name = PREDICATE_PARAMETER_NAME, required = false) String predicate,
            @RequestParam(name = OBJECT_PARAMETER_NAME, required = false) String object,
            @RequestParam(name = GRAPH_PARAMETER_NAME, required = false) String graph,
            @RequestParam(name = PAGE_NUMBER_PARAMETER_NAME, required = false) Integer pageNumber,
            HttpServletRequest request) {
        var model = ModelFactory.createDefaultModel();

        var fragmentIRI = fragmentIRI(request);
        var fragmentMetadata = model.createResource(fragmentIRI + "#metadata");
        var fragment = model.createResource(fragmentIRI);
        var fragmentDataset = model.createResource(baseUrl + request.getRequestURI() + "#dataset");

        var dataset = model.createResource(baseUrl + "#dataset")
                            .addProperty(HYDRA_MEMBER, fragmentDataset)
                            .addProperty(RDF.type, HYDRA_COLLECTION);

        ofNullable(datasetName).ifPresent(name -> dataset.addProperty(DC.title, name));
        ofNullable(datasetDescription).ifPresent(description -> dataset.addProperty(DC.description, description));

        var response = resolver(false).linkedDataFragment(
                                                            ofNullable(subject),
                                                            ofNullable(predicate),
                                                            ofNullable(object),
                                                            ofNullable(graph),
                                                            ofNullable(pageNumber));

        var templateAndMapping = model.createResource()
                .addProperty(HYDRA_TEMPLATE, template(request))
                .addProperty(HYDRA_VARIABLE_REPRESENTATION, HYDRA_EXPLICIT_REPRESENTATION)
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
                                .addProperty(HYDRA_PROPERTY, RDF.object));

        fragmentMetadata.addProperty(FOAF.primaryTopic, model.createResource(fragment));

        fragmentDataset.addProperty(RDF.type, VOID.Dataset)
                       .addProperty(RDF.type, HYDRA_COLLECTION)
                       .addProperty(VOID.subset, fragment)
                       .addProperty(HYDRA_SEARCH, templateAndMapping);

        var fragmentCardinality = response.getFragmentCardinality()
                .map(model::createTypedLiteral)
                .orElseGet( () -> model.createTypedLiteral(0L));

        fragment.addProperty(VOID.subset, fragment)
                .addProperty(RDF.type, HYDRA_PARTIAL_COLLECTION)
                .addProperty(DC.source, fragmentDataset.getURI())
                .addLiteral(HYDRA_TOTAL_ITEMS, fragmentCardinality)
                .addLiteral(VOID.triples, fragmentCardinality);

        if (response.isPaged()) {
            fragmentDataset.addProperty(RDF.type, HYDRA_PAGED_COLLECTION);
            fragment.addProperty(RDF.type, HYDRA_PAGED_COLLECTION);
        }

        ofNullable(datasetName).ifPresent(name -> {
            fragment.addProperty(DC.title, "Linked Data Fragment of " + name , "en");
            fragment.addProperty(DC.description, "Linked Data Fragment of Share-VDE dataset containing triples matching the pattern " + pattern(request), "en");
        });

        return DatasetFactory.create()
                    .setDefaultModel(response.patternSolution())
                    .addNamedModel(fragmentMetadata.getURI(), withControls(model, response, fragment, ofNullable(pageNumber)));
    }

    public Model withControls(Model model,
                              LinkedDataFragmentResponse<?> response,
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
            if (currentPageNumber != lastPageNumber) {
                var nextPageId = model.createResource(fragmentUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, Long.toString(currentPageNumber + 1)).toString());
                fragment.addProperty(NEXT_PAGE, nextPageId);

                var lastPageId = model.createResource(fragmentUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, Long.toString(lastPageNumber)).toString());
                fragment.addProperty(LAST_PAGE, lastPageId);
            }
        }
        return model;
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

    @SneakyThrows
    private URIBuilder url(String baseUrl) {
        return new URIBuilder(baseUrl);
    }

    private String template(HttpServletRequest request) {
        return baseUrl.replace("/fragments", "") +
                request.getRequestURI() +
                "{?" +
                    SUBJECT_PARAMETER_NAME + "," +
                    PREDICATE_PARAMETER_NAME + "," +
                    OBJECT_PARAMETER_NAME + "," +
                    "page" +
                "}";
    }

    private String pattern(HttpServletRequest request) {
        return Stream.of(ofNullable(request.getParameter(SUBJECT_PARAMETER_NAME)).orElse("?s"),
                        ofNullable(request.getParameter(PREDICATE_PARAMETER_NAME)).orElse("?p"),
                        ofNullable(request.getParameter(OBJECT_PARAMETER_NAME)).orElse("?o"),
                        ofNullable(request.getParameter(GRAPH_PARAMETER_NAME)).orElse("?q"))
                .filter(Objects::nonNull)
                .collect(joining(" ", "{", "}"));
    }

    private String fragmentIRI(HttpServletRequest request) {
        var iri = baseUrl +
                request.getRequestURI() +
                Stream.of(ofNullable(request.getParameter(SUBJECT_PARAMETER_NAME))
                                            .map(v -> URLEncoder.encode(v, StandardCharsets.UTF_8))
                                            .map(v -> SUBJECT_PARAMETER_NAME + "=" + v).orElse(null),
                                ofNullable(request.getParameter(PREDICATE_PARAMETER_NAME))
                                            .map(v -> URLEncoder.encode(v, StandardCharsets.UTF_8))
                                            .map(v -> PREDICATE_PARAMETER_NAME + "=" + v).orElse(null),
                                ofNullable(request.getParameter(OBJECT_PARAMETER_NAME))
                                            .map(v -> URLEncoder.encode(v, StandardCharsets.UTF_8))
                                            .map(v -> OBJECT_PARAMETER_NAME + "=" + v).orElse(null),
                                ofNullable(request.getParameter(GRAPH_PARAMETER_NAME))
                                            .map(v -> URLEncoder.encode(v, StandardCharsets.UTF_8))
                                            .map(v -> GRAPH_PARAMETER_NAME + "=" + v).orElse(null),
                                ofNullable(request.getParameter(PAGE_NUMBER_PARAMETER_NAME))
                                            .map(v -> PAGE_NUMBER_PARAMETER_NAME + "=" + v).orElse(null))
                        .filter(Objects::nonNull)
                        .collect(joining("&", "?", ""));
        return iri.endsWith("?") ? iri.substring(0, iri.length() - 1) : iri;
    }
}
