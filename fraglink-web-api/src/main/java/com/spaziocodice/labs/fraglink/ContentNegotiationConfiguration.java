package com.spaziocodice.labs.fraglink;

import com.spaziocodice.labs.fraglink.converters.RDFMessageConverter;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.jena.riot.Lang.JSONLD;

@AutoConfiguration
public class ContentNegotiationConfiguration implements WebMvcConfigurer {
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
    }};

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.useRegisteredExtensionsOnly(true)
                  .defaultContentType(MediaType.parseMediaType(WebContent.contentTypeTriG))
                  .mediaTypes(MEDIA_TYPES);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        createConverters(List.of(WebContent.contentTypeNTriplesAlt,
                                 WebContent.contentTypeN3Alt1,
                                 WebContent.contentTypeN3Alt2,
                                 WebContent.contentTypeNQuadsAlt1,
                                 WebContent.contentTypeTriGAlt1,
                                 WebContent.contentTypeTriX,
                                 WebContent.contentTypeTriXxml))
                .forEach(converter -> converters.add(0, converter));

        createConverters(SUPPORTED_TRIPLES_MEDIA_TYPES.values()
                .stream()
                .map(Object::toString)
                .toList()).forEach(converter -> converters.add(0, converter));


        converters.addAll(createConverters(SUPPORTED_QUADS_MEDIA_TYPES.values()
                                                .stream()
                                                .map(Object::toString)
                                                .toList()));
    }

    public static List<RDFMessageConverter> createConverters(List<String> mediaTypes) {
        return mediaTypes.stream().map(RDFMessageConverter::new).collect(Collectors.toList());
    }
}