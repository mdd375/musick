package ru.m0vt.musick.exception;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import ru.m0vt.musick.dto.ErrorResponseDTO;

@Hidden
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponseDTO> handleBaseException(
        BaseException ex,
        WebRequest request
    ) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            ex.getMessage(),
            ex.getErrorCode(),
            ex.getStatus().value()
        );
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
        MethodArgumentNotValidException ex
    ) {
        String errorMessage = ex
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            errorMessage,
            "VALIDATION_ERROR",
            HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponseDTO> handleAllOtherExceptions(
        Exception ex,
        WebRequest request
    ) {
        // Здесь можно логировать неожиданные ошибки
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
            "An unexpected error occurred",
            "INTERNAL_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(
            errorResponse,
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
