package com.opentms.basedata.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Arrays;

@Component
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonJsonBodyWriter implements MessageBodyWriter<Object> {

    private final ObjectMapper objectMapper;

    public JacksonJsonBodyWriter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, java.lang.annotation.Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public void writeTo(Object object, Class<?> type, Type genericType, java.lang.annotation.Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        objectMapper.writeValue(entityStream, object);
    }

    @Override
    public long getSize(Object object, Class<?> type, Type genericType, java.lang.annotation.Annotation[] annotations, MediaType mediaType) {
        return -1;
    }
}