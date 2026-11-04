package ru.naboyshchikovfp.tour_agency.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import ru.naboyshchikovfp.tour_agency.exceptions.NotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Показывает страницу 404 при отсутствии запрошенной записи.
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(
            NotFoundException exception,
            Model model
    ) {
        model.addAttribute("message", exception.getMessage());

        return "error/404";
    }
}