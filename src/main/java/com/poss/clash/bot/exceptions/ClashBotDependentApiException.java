package com.poss.clash.bot.exceptions;

import org.springframework.http.HttpStatus;

public class ClashBotDependentApiException extends HttpResponseException {

  public ClashBotDependentApiException(String message, Throwable exception, HttpStatus httpStatus) {
    super(message, exception, httpStatus);
  }

}
