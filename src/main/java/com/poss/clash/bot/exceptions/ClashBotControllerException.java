package com.poss.clash.bot.exceptions;

import org.springframework.http.HttpStatus;

public class ClashBotControllerException extends HttpResponseException {

    public ClashBotControllerException(String message, Throwable exception, HttpStatus httpStatus) {
        super(message, exception, httpStatus);
    }

    public ClashBotControllerException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

}
