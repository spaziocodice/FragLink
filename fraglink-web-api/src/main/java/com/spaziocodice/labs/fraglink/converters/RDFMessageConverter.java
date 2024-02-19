package com.spaziocodice.labs.fraglink.converters;

import com.spaziocodice.labs.fraglink.ContentNegotiationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.Type;

@Slf4j
public class RDFMessageConverter extends AbstractGenericHttpMessageConverter<Dataset> {

    private final Lang syntax;

    public RDFMessageConverter(String mediaType) {
        super(MediaType.parseMediaType(mediaType));
        this.syntax = WebContent.contentTypeXML.equals(mediaType)
                            ? Lang.TRIG
                            : RDFLanguages.contentTypeToLang(mediaType);
    }

    @Override
    protected void writeInternal(Dataset dataset, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        if (ContentNegotiationConfiguration.QUADS_SYNTAXES.contains(syntax)) {
            RDFDataMgr.write(outputMessage.getBody(), dataset, syntax);
        } else if (ContentNegotiationConfiguration.TRIPLES_SYNTAXES.contains(syntax)) {
            var model = ModelFactory.createDefaultModel().add(dataset.getUnionModel()).add(dataset.getDefaultModel());
            RDFDataMgr.write(outputMessage.getBody(), model, syntax);
        } else {
            throw new HttpMessageNotWritableException("Malformed Syntax / Response combination.");
        }
    }

    @Override
    protected Dataset readInternal(Class<? extends Dataset> clazz, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("Inbound messages in " + syntax + " syntax are not supported.", inputMessage);
    }

    @Override
    public Dataset read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("Inbound messages in " + syntax + " syntax are not supported.", inputMessage);
    }
}
