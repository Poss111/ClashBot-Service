package com.poss.clash.bot.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class HttpResponseException extends Throwable {

    private HttpStatus httpStatus;

    HttpResponseException(String message, Throwable exception, HttpStatus httpStatus) {
        super(message, exception);
        this.httpStatus = httpStatus;
    }

    HttpResponseException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

}
