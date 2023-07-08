package com.poss.clash.bot.exceptions;

import org.springframework.http.HttpStatus;

public class ClashBotDbException extends HttpResponseException {

  public ClashBotDbException(String message, Throwable exception, HttpStatus httpStatus) {
    super(message, exception, httpStatus);
  }

  public ClashBotDbException(String message, HttpStatus httpStatus) {
    super(message, httpStatus);
  }

}
