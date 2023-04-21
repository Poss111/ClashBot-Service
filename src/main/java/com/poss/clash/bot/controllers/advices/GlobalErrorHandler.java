package com.poss.clash.bot.controllers.advices;

import com.poss.clash.bot.exceptions.HttpResponseException;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Order(-2)
public class GlobalErrorHandler extends AbstractErrorWebExceptionHandler {

    public GlobalErrorHandler(ErrorAttributes errorAttributes, WebProperties webProperties, ApplicationContext applicationContext,
                              ServerCodecConfigurer configurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(
                RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(
            ServerRequest request) {
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request,
                ErrorAttributeOptions.defaults());

        HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        Throwable error = getError(request);

        if (error instanceof HttpResponseException
                && null != ((HttpResponseException) error).getHttpStatus()) {
            statusCode = ((HttpResponseException) error).getHttpStatus();
            errorPropertiesMap.put("status", ((HttpResponseException) error).getHttpStatus().value());
            errorPropertiesMap.put("error", statusCode.getReasonPhrase());
            errorPropertiesMap.put("message", error.getMessage());
        }

        return ServerResponse.status(statusCode)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorPropertiesMap));
    }
}
