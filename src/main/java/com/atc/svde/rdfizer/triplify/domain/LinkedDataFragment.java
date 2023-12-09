package com.atc.svde.rdfizer.triplify.domain;

import com.atc.svde.rdfizer.triplify.Identifiers;
import lombok.Builder;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VOID;

import static com.atc.svde.rdfizer.triplify.Identifiers.HYDRA_MAPPING;
import static com.atc.svde.rdfizer.triplify.Identifiers.HYDRA_PROPERTY;
import static com.atc.svde.rdfizer.triplify.Identifiers.HYDRA_TEMPLATE;
import static com.atc.svde.rdfizer.triplify.Identifiers.HYDRA_VARIABLE;
import static com.atc.svde.rdfizer.triplify.Identifiers.PARAMETERNAME_OBJ;
import static com.atc.svde.rdfizer.triplify.Identifiers.PAGE_NUMBER_PARAMETER_NAME;
import static com.atc.svde.rdfizer.triplify.Identifiers.PARAMETERNAME_PRED;
import static com.atc.svde.rdfizer.triplify.Identifiers.PARAMETERNAME_SUBJ;
import static com.atc.svde.rdfizer.triplify.Identifiers.RDF_OBJECT;
import static com.atc.svde.rdfizer.triplify.Identifiers.RDF_PREDICATE;
import static com.atc.svde.rdfizer.triplify.Identifiers.RDF_SUBJECT;

/**
 * A Linked Data Fragment (LDF) of a Linked Data Dataset encapsulates
 * <ul>
 *     <li>Data (triples or quads) matching a specific selector</li>
 *     <li>Metadata</li>
 *     <li>Hypermedia controls</li>
 * </ul>
 */
@Getter
@Builder
public abstract class LinkedDataFragment<T> {
    private final String fragmentURI;
    private final String datasetURI;

    private final long pageNumber;
    private final T matches;

    private final Model model;

    public LinkedDataFragment(String fragmentURI, String datasetURI, long pageNumber, T matches) {
        this.fragmentURI = fragmentURI;
        this.datasetURI = datasetURI;
        this.pageNumber = pageNumber;
        this.matches = matches;

        this.model = withControls(withMetadata(ModelFactory.createDefaultModel()));
    }

    private Model withMetadata(Model model) {
        var fragmentId = model.createResource(fragmentURI)
                            .addProperty(RDF.type, Identifiers.HYDRA_COLLECTION )
                            .addProperty(RDF.type, Identifiers.HYDRA_PAGEDCOLLECTION );

        model.createResource(datasetURI)
                .addProperty(RDF.type, VOID.Dataset)
                .addProperty(RDF.type, Identifiers.HYDRA_COLLECTION )
                .addProperty(VOID.subset, fragmentId )
                .addProperty(Identifiers.HYDRA_ITEMSPERPAGE, model.createTypedLiteral(100));

        return model;
    }

    @SneakyThrows
    private URIBuilder url(String baseUrl) {
        return new URIBuilder(baseUrl);
    }

    public Model withControls(Model model) {
        var pageUrl = url(fragmentURI);
        var fragmentId = model.createResource( fragmentURI );

        var firstPageId = model.createResource(pageUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME, "1").toString());

        fragmentId.addProperty( Identifiers.HYDRA_FIRSTPAGE, firstPageId );

        if ( pageNumber > 1) {
            final String prevPageNumber = Long.toString( pageNumber - 1 );
            final Resource prevPageId =
                    model.createResource(
                            pageUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME,
                                    prevPageNumber).toString() );

            fragmentId.addProperty( Identifiers.HYDRA_PREVIOUSPAGE, prevPageId );
        }

        if ( ! isLastPage ) {
            final String nextPageNumber = Long.toString( pageNumber + 1 );
            final Resource nextPageId =
                    model.createResource(
                            pageUrl.setParameter(PAGE_NUMBER_PARAMETER_NAME,
                                    nextPageNumber).toString() );

            fragmentId.addProperty( Identifiers.HYDRA_NEXTPAGE, nextPageId );
        }

        final Resource datasetId = model.createResource( getDatasetURI() );

        final Resource triplePattern = model.createResource();
        final Resource subjectMapping = model.createResource();
        final Resource predicateMapping = model.createResource();
        final Resource objectMapping = model.createResource();

        datasetId.addProperty( Identifiers.HYDRA_SEARCH, triplePattern );

        triplePattern.addProperty(HYDRA_TEMPLATE, getTemplate() );
        triplePattern.addProperty(HYDRA_MAPPING, subjectMapping );
        triplePattern.addProperty(HYDRA_MAPPING, predicateMapping );
        triplePattern.addProperty(HYDRA_MAPPING, objectMapping );

        subjectMapping.addProperty(HYDRA_VARIABLE, PARAMETERNAME_SUBJ );
        subjectMapping.addProperty(HYDRA_PROPERTY, RDF_SUBJECT );

        predicateMapping.addProperty( HYDRA_VARIABLE, PARAMETERNAME_PRED );
        predicateMapping.addProperty( HYDRA_PROPERTY, RDF_PREDICATE );

        objectMapping.addProperty( HYDRA_VARIABLE, PARAMETERNAME_OBJ );
        objectMapping.addProperty( HYDRA_PROPERTY, RDF_OBJECT );
    }

    private void addMetadata( final Model model ) {



    }
}
