package com.spaziocodice.labs.fraglink.controllers;

import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VOID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.spaziocodice.labs.fraglink.Identifiers.FIRST_PAGE;
import static com.spaziocodice.labs.fraglink.Identifiers.GRAPH_PARAMETER_NAME;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_COLLECTION;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_MAPPING;
import static com.spaziocodice.labs.fraglink.Identifiers.HYDRA_PAGEDCOLLECTION;
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
import static com.spaziocodice.labs.fraglink.Identifiers.SD;
import static com.spaziocodice.labs.fraglink.Identifiers.SUBJECT_PARAMETER_NAME;
import static com.spaziocodice.labs.fraglink.Identifiers.TOTAL_MATCHES;
import static java.util.Optional.of;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class LinkedDataFragment {

    @Value("${fraglink.page.maxStatements:50}")
    private int maxStatementsInPage;

    private final static String TEMPLATE = of(methodOn(LinkedDataFragment.class))
            .map(controller -> controller.linkedDataFragment("","","","", null))
            .map(WebMvcLinkBuilder::linkTo)
            .map(linkBuilder -> linkBuilder +
                    "{" +
                        SUBJECT_PARAMETER_NAME + "," +
                        PREDICATE_PARAMETER_NAME + "," +
                        OBJECT_PARAMETER_NAME + "," +
                        GRAPH_PARAMETER_NAME + "," +
                    "}")
            .orElseThrow();
    @GetMapping("/fragment")
    public Dataset linkedDataFragment(
                    @RequestParam(name = SUBJECT_PARAMETER_NAME, required = false) String subject,
                    @RequestParam(name = PREDICATE_PARAMETER_NAME, required = false) String predicate,
                    @RequestParam(name = OBJECT_PARAMETER_NAME, required = false) String object,
                    @RequestParam(name = GRAPH_PARAMETER_NAME, required = false) String graph,
                    @RequestParam(name = PAGE_NUMBER_PARAMETER_NAME, required = false) Integer pageNumber) {
        var fragmentUri = "http://blablabl.com";//linkTo(methodOn(LinkedDataFragment.class).linkedDataFragment(subject, predicate, object, graph, pageNumber)).toString();

        final Model model =
                withControls(
                        withMetadata(ModelFactory.createDefaultModel(), fragmentUri, fragmentUri, 0),
                        fragmentUri,
                        pageNumber,
                        0,
                        fragmentUri);
        return DatasetFactory.create(model);
    }

    private Model withMetadata(Model model, String fragmentUri, String datasetUri, long totalMatches) {
        var fragmentId = model.createResource(fragmentUri)
                            .addProperty(RDF.type, HYDRA_COLLECTION )
                            .addProperty(RDF.type, HYDRA_PAGEDCOLLECTION );

        model.createResource(datasetUri)
                .addProperty(RDF.type, VOID.Dataset)
                .addProperty(RDF.type, HYDRA_COLLECTION )
                .addProperty(VOID.subset, fragmentId )
                .addLiteral(TOTAL_MATCHES, model.createTypedLiteral(totalMatches))
                .addProperty(MAX_STATEMENTS_IN_PAGE, model.createTypedLiteral(maxStatementsInPage));

        return model;
    }

    public Model withControls(Model model, String fragmentUri, Integer inputPageNumber, long totalMatches, String datasetUri) {
        var pageUrl = url(fragmentUri);
        var firstPageUri = model.createResource(pageUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, "1").toString());

        var fragmentId = model.createResource(fragmentUri);
        fragmentId.addProperty(FIRST_PAGE, firstPageUri);

        var pageNumber = inputPageNumber != null ? inputPageNumber.intValue() : 1;
        if (pageNumber > 1) {
            var prevPageNumber = Long.toString(pageNumber - 1);
            var prevPageId = model.createResource(pageUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, prevPageNumber).toString());
            fragmentId.addProperty(PREVIOUS_PAGE, prevPageId );
        }

        var lastPageNumber = (totalMatches / maxStatementsInPage) + 1;
        if (pageNumber != lastPageNumber) {
            var nextPageId = model.createResource(pageUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, Long.toString(pageNumber + 1)).toString());
            fragmentId.addProperty(NEXT_PAGE, nextPageId);

            var lastPageId = model.createResource(pageUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, Long.toString(lastPageNumber)).toString());
            fragmentId.addProperty(LAST_PAGE, lastPageId);
        }

        var triplePattern = model.createResource()
                                .addProperty(HYDRA_TEMPLATE, TEMPLATE)
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
}
