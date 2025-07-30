package org.GoLifeAPI.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

@ControllerAdvice
public class EmptyRequestBodyHandler extends RequestBodyAdviceAdapter {

    private final ObjectMapper objectMapper;

    public EmptyRequestBodyHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter parameter,
                            Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return parameter.hasParameterAnnotation(RequestBody.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage,
                                           MethodParameter parameter,
                                           Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        byte[] bodyBytes = StreamUtils.copyToByteArray(inputMessage.getBody());
        if (bodyBytes.length > 0) {
            try {
                JsonNode node = objectMapper.readTree(bodyBytes);
                if (node.isObject() && node.isEmpty()) {
                    throw new HttpMessageNotReadableException(
                            "El cuerpo no puede estar vacío", inputMessage);
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new HttpMessageNotReadableException("", e);
            }
        }

        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(bodyBytes);
            }

            @Override
            public HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }
        };
    }
}