package com.hillel.items_exchange.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDtoException extends Exception {

    public InvalidDtoException(String message) {
        super(message);
    }
}
