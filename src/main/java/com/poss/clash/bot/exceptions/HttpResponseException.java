package com.poss.clash.bot.exceptions;

import org.springframework.http.HttpStatus;

public class HttpResponseException extends Throwable {

    private final HttpStatus httpStatus;

    HttpResponseException(String message, Throwable exception, HttpStatus httpStatus) {
        super(message, exception);
        this.httpStatus = httpStatus;
    }

    HttpResponseException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
