package ru.naboyshchikovfp.tour_agency.exceptions;

public class ValidationException extends RuntimeException{
    private final String field;

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return this.field;
    }
}
