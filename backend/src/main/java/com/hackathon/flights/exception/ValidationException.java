package com.hackathon.flights.exception;

public class ValidationException extends RuntimeException {

    private final String errorCode;

    public ValidationException(String mensaje, String errorCode, Throwable causa) {
        super(mensaje, causa);
        this.errorCode = errorCode;
    }

    public ValidationException(String mensaje, String errorCode) {
        this(mensaje, errorCode, null);
    }

    public ValidationException(String mensaje) {
        this(mensaje, "VALIDACION_FALLIDA", null);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
