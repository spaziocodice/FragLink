package com.spaziocodice.labs.fraglink;

import org.apache.jena.rdf.model.Property;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;

public interface Identifiers {
    String SUBJECT_PARAMETER_NAME = "subject";
    String PREDICATE_PARAMETER_NAME = "predicate";
    String OBJECT_PARAMETER_NAME = "object";
    String GRAPH_PARAMETER_NAME = "graph";
    String HYDRA = "http://www.w3.org/ns/hydra/core#";
    String SD = "http://www.w3.org/ns/sparql-service-description#";

    Property HYDRA_TOTALITEMS = createProperty(HYDRA + "totalItems");

    Property MAX_STATEMENTS_IN_PAGE = createProperty(HYDRA + "itemsPerPage");

    Property HYDRA_SEARCH = createProperty(HYDRA + "search");

    Property HYDRA_TEMPLATE = createProperty(HYDRA + "template");

    Property HYDRA_MAPPING = createProperty(HYDRA + "mapping");

    Property HYDRA_VARIABLE = createProperty(HYDRA + "variable");

    Property HYDRA_PROPERTY = createProperty(HYDRA + "property");

    Property HYDRA_COLLECTION = createProperty(HYDRA + "Collection");

    Property HYDRA_PAGEDCOLLECTION = createProperty(HYDRA + "PagedCollection");

    Property HYDRA_FIRSTPAGE = createProperty(HYDRA + "firstPage");

    Property HYDRA_NEXTPAGE = createProperty(HYDRA + "nextPage");

    Property HYDRA_PREVIOUSPAGE = createProperty(HYDRA + "previousPage");

    String PAGE_NUMBER_PARAMETER_NAME = "page";
}
