package com.atc.svde.rdfizer.triplify;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.web.config.SpringDataWebConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.atc.svde.rdfizer.triplify.converters.RDFMessageConverter.createConverters;
import static org.apache.jena.riot.Lang.JSONLD;

@Configuration
public class ContentNegotiationConfiguration extends SpringDataWebConfiguration {
    @Value("${svde.baseuri}")
    private String baseUri;

    private final static Map<String, MediaType> SUPPORTED_TRIPLES_MEDIA_TYPES = new HashMap<>() {{
        put("xml", MediaType.parseMediaType(WebContent.contentTypeRDFXML));
        put("rdf", MediaType.parseMediaType(WebContent.contentTypeRDFXML));
        put("ttl", MediaType.parseMediaType(WebContent.contentTypeTurtle));
        put("n3", MediaType.parseMediaType(WebContent.contentTypeN3));
        put("nt", MediaType.parseMediaType(WebContent.contentTypeNTriples));
        put(JSONLD.getName(), MediaType.parseMediaType(WebContent.contentTypeJSONLD));
        put("rt", MediaType.parseMediaType(WebContent.contentTypeRDFThrift));
        put("trdf", MediaType.parseMediaType(WebContent.contentTypeRDFThrift));
    }};

    private final static Map<String, MediaType> SUPPORTED_QUADS_MEDIA_TYPES = new HashMap<>() {{
        put("nq", MediaType.parseMediaType(WebContent.contentTypeNQuads));
        put("trig", MediaType.parseMediaType(WebContent.contentTypeTriG));
        put("trix", MediaType.parseMediaType(WebContent.contentTypeTriX));
    }};

    public final static Set<Lang> TRIPLES_SYNTAXES =
            SUPPORTED_TRIPLES_MEDIA_TYPES.values().stream()
                    .map(Object::toString)
                    .map(RDFLanguages::contentTypeToLang)
                    .collect(Collectors.toSet());

    public final static Set<Lang> QUADS_SYNTAXES =
            SUPPORTED_QUADS_MEDIA_TYPES.values().stream()
                    .map(Object::toString)
                    .map(RDFLanguages::contentTypeToLang)
                    .collect(Collectors.toSet());

    private final static Map<String, MediaType> MEDIA_TYPES= new HashMap<>() {{
        putAll(SUPPORTED_TRIPLES_MEDIA_TYPES);
        putAll(SUPPORTED_QUADS_MEDIA_TYPES);
        put("json", MediaType.APPLICATION_JSON);
        put("marcxml", MediaType.parseMediaType("application/marcxml+xml"));
        put("mrc", MediaType.parseMediaType("application/marc"));
        put("ris", MediaType.parseMediaType("application/x-research-info-systems"));
    }};

    public static final List<MediaType> RDF_MEDIA_TYPES = new ArrayList<>() {{
        addAll(SUPPORTED_TRIPLES_MEDIA_TYPES.values());
        addAll(SUPPORTED_QUADS_MEDIA_TYPES.values());
    }};

    public ContentNegotiationConfiguration(ApplicationContext context, ObjectFactory<ConversionService> conversionService) {
        super(context, conversionService);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(true)
                .favorParameter(true)
                .ignoreAcceptHeader(false)
                .useRegisteredExtensionsOnly(true)
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaTypes(MEDIA_TYPES);
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(true);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        createConverters(List.of(WebContent.contentTypeNTriplesAlt,
                                 WebContent.contentTypeN3Alt1,
                                 WebContent.contentTypeN3Alt2,
                                 WebContent.contentTypeNQuadsAlt1,
                                 WebContent.contentTypeTriGAlt1)).forEach(converter -> converters.add(0, converter));

        createConverters(SUPPORTED_TRIPLES_MEDIA_TYPES.values()
                            .stream()
                            .map(Object::toString)
                            .toList()).forEach(converter -> converters.add(0, converter));

        converters.addAll(createConverters(SUPPORTED_QUADS_MEDIA_TYPES.values()
                                                .stream()
                                                .map(Object::toString)
                                                .toList()));
    }
}