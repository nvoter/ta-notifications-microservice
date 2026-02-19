package org.fcs.notifications.microservice.controllers;

import lombok.extern.slf4j.Slf4j;
import org.fcs.notifications.microservice.dtos.ApiErrorResponse;
import org.fcs.notifications.microservice.exceptions.NotificationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotificationNotFound(
            NotificationNotFoundException e
    ) {
        log.warn("Не найдено: {}", e.getMessage());
        return buildError(HttpStatus.NOT_FOUND, e.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(
                        status.value(),
                        message
                ));
    }
}
