package com.gleipnirymir.exceptions;

public class CrthExceptionFactory {


    public static CrthException responseException(String message) {
        return new CrthException(message);
    }

}
