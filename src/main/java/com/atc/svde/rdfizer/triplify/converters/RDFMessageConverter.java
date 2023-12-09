package com.atc.svde.rdfizer.triplify.converters;

import com.atc.svde.rdfizer.triplify.ContentNegotiationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.Dataset;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class RDFMessageConverter extends AbstractGenericHttpMessageConverter<Object> {

    private final Lang syntax;

    public RDFMessageConverter(String mediaType) {
        super(MediaType.parseMediaType(mediaType));
        if ("application/xml".equals(mediaType)) {
            syntax = Lang.RDFXML;
        } else {
            syntax = RDFLanguages.contentTypeToLang(mediaType);
        }}

    @Override
    protected void writeInternal(Object input, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        if (input instanceof Dataset) {
            var dataset = (Dataset) input;
            if (ContentNegotiationConfiguration.TRIPLES_SYNTAXES.contains(syntax)) {
                RDFDataMgr.write(outputMessage.getBody(), dataset.getUnionModel(), syntax);
            } else if (ContentNegotiationConfiguration.QUADS_SYNTAXES.contains(syntax)) {
                RDFDataMgr.write(outputMessage.getBody(), dataset, syntax);
            } else {
                throw new HttpMessageNotWritableException("Malformed Syntax / Response combination.");
            }
        } else if (input instanceof String) {
            outputMessage.getBody().write(((String)input).getBytes(StandardCharsets.UTF_8));
        } else {
            throw new HttpMessageNotWritableException("Content Not Writable.");
        }
    }

    @Override
    protected Dataset readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("Inbound messages in " + syntax + " syntax are not supported.", inputMessage);
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("Inbound messages in " + syntax + " syntax are not supported.", inputMessage);
    }

    public static List<RDFMessageConverter> createConverters(List<String> mediaTypes) {
        return mediaTypes.stream().map(RDFMessageConverter::new).collect(Collectors.toList());
    }
}
