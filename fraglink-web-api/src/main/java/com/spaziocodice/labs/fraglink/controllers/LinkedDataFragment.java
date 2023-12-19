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
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VOID;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    }

    @GetMapping("/")
    public Dataset linkedDataFragment(
            @RequestParam(name = SUBJECT_PARAMETER_NAME, required = false) String subject,
            @RequestParam(name = PREDICATE_PARAMETER_NAME, required = false) String predicate,
            @RequestParam(name = OBJECT_PARAMETER_NAME, required = false) String object,
            @RequestParam(name = GRAPH_PARAMETER_NAME, required = false) String graph,
            @RequestParam(name = PAGE_NUMBER_PARAMETER_NAME, required = false) Integer pageNumber,
            HttpServletRequest request) {
        var datasetUri = baseUrl + "#dataset";
        var metadataUri = baseUrl + "#metadata";
        var fragmentUri = baseUrl + request.getRequestURI() + ofNullable(request.getQueryString()).map(value -> "?" + value).orElse("");

        var response = resolver(false).linkedDataFragment(
                                                            ofNullable(subject),
                                                            ofNullable(predicate),
                                                            ofNullable(object),
                                                            ofNullable(graph),
                                                            ofNullable(pageNumber));
        return DatasetFactory.create()
                    .setDefaultModel(response.patternSolution())
                    .addNamedModel(metadataUri,
                                    withControls(
                                            withMetadata(ModelFactory.createDefaultModel(),
                                                         response,
                                                         datasetUri, metadataUri, fragmentUri),
                                            response,
                                            datasetUri,
                                            fragmentUri));
    }

    private Model withMetadata(
            Model model,
            LinkedDataFragmentResponse<?> response,
            String datasetUri,
            String metadataUri,
            String fragmentUri) {
        model.createResource(metadataUri).addProperty(FOAF.primaryTopic, model.createProperty(datasetUri));

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
                              String datasetUri,
                              String fragmentUri) {
        var fragmentUrl = url(fragmentUri);
        var fragment = model.createResource(fragmentUri);
        var firstPageUri = model.createResource(fragmentUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, "1").toString());

        fragment.addProperty(FIRST_PAGE, firstPageUri);

        final int pageNumber = response.getPageNumber().orElse(1);
        if (pageNumber > 1) {
            var prevPageNumber = Long.toString(pageNumber - 1);
            var prevPageId = model.createResource(fragmentUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, prevPageNumber).toString());
            fragment.addProperty(PREVIOUS_PAGE, prevPageId);
        }

        if (response.getFragmentCardinality().isPresent()) {
            var lastPageNumber = (response.getFragmentCardinality().get() / maxStatementsInPage) + 1;
            var nextPageId = model.createResource(fragmentUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, Long.toString(pageNumber + 1)).toString());
            fragment.addProperty(NEXT_PAGE, nextPageId);
            if (pageNumber != lastPageNumber) {
                var lastPageId = model.createResource(fragmentUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, Long.toString(lastPageNumber)).toString());
                fragment.addProperty(LAST_PAGE, lastPageId);
            }
        }

        var triplePattern = model.createResource()
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

        model.createResource(datasetUri).addProperty(HYDRA_SEARCH, triplePattern );
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
