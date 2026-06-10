package com.example.propertyrentalmanagement.exceptions;

import com.example.propertyrentalmanagement.dto.response.GenericResponse;
import com.example.propertyrentalmanagement.utils.ErrorTool;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ErrorTool errorTool;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return GenericResponse.builder()
                .data(errorTool.mapErrors(ex.getBindingResult().getFieldErrors()))
                .status(HttpStatus.BAD_REQUEST)
                .build().buildResponse();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<GenericResponse> userNotFoundException(UserNotFoundException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                java.time.LocalDateTime.now(),
                ex.getMessage()
        );
        return GenericResponse.builder()
                .data(errorResponse)
                .status(HttpStatus.NOT_FOUND).build().buildResponse();
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<GenericResponse> userAlreadyExistsException(UserAlreadyExistsException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                java.time.LocalDateTime.now(),
                ex.getMessage()
        );
        return GenericResponse.builder()
                .data(errorResponse)
                .status(HttpStatus.CONFLICT).build().buildResponse();
    }

    @ExceptionHandler(InvalidCredentials.class)
    public ResponseEntity<GenericResponse> invalidCredentials(InvalidCredentials ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                java.time.LocalDateTime.now(),
                ex.getMessage()
        );
        return GenericResponse.builder()
                .data(errorResponse)
                .status(HttpStatus.UNAUTHORIZED).build().buildResponse();
    }

    @ExceptionHandler(PropertyNotFound.class)
    public ResponseEntity<GenericResponse> propertyNotFound(PropertyNotFound ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                java.time.LocalDateTime.now(),
                ex.getMessage()
        );
        return GenericResponse.builder()
                .data(errorResponse)
                .status(HttpStatus.NOT_FOUND).build().buildResponse();
    }

    @ExceptionHandler(MaintenanceNotFoundException.class)
    public ResponseEntity<GenericResponse> maintenanceNotFoundException(MaintenanceNotFoundException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                java.time.LocalDateTime.now(),
                ex.getMessage()
        );
        return GenericResponse.builder()
                .data(errorResponse)
                .status(HttpStatus.NOT_FOUND).build().buildResponse();
    }

    @ExceptionHandler(NotResourceOwnerException.class)
    public ResponseEntity<GenericResponse> notResourceOwnerException(NotResourceOwnerException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                java.time.LocalDateTime.now(),
                ex.getMessage()
        );
        return GenericResponse.builder()
                .data(errorResponse)
                .status(HttpStatus.FORBIDDEN).build().buildResponse();
    }

    @ExceptionHandler(MaintenanceScheduleNotFoundException.class)
    public ResponseEntity<GenericResponse> maintenanceScheduleNotFoundException(MaintenanceScheduleNotFoundException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                java.time.LocalDateTime.now(),
                ex.getMessage()
        );
        return GenericResponse.builder()
                .data(errorResponse)
                .status(HttpStatus.NOT_FOUND).build().buildResponse();
    }
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<GenericResponse> reservationNotFoundException(ReservationNotFoundException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                java.time.LocalDateTime.now(),
                ex.getMessage()
        );

        return GenericResponse.builder()
                .data(errorResponse)
                .status(HttpStatus.NOT_FOUND)
                .build()
                .buildResponse();
    }

    @ExceptionHandler(AccessCodeNotFoundException.class)
    public ResponseEntity<GenericResponse> accessCodeNotFoundException(AccessCodeNotFoundException ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                java.time.LocalDateTime.now(),
                ex.getMessage()
        );

        return GenericResponse.builder()
                .data(errorResponse)
                .status(HttpStatus.NOT_FOUND)
                .build()
                .buildResponse();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse> illegalArgumentException(IllegalArgumentException ex) {
         CustomErrorResponse errorResponse = new CustomErrorResponse(
                 java.time.LocalDateTime.now(),
                 ex.getMessage()
         );
      
          return GenericResponse.builder()
                  .data(errorResponse)
                  .status(HttpStatus.BAD_REQUEST)
                  .build()
                  .buildResponse();
    }
  
    @ExceptionHandler(InvalidReservationCancellationException.class)
    public ResponseEntity<GenericResponse> invalidReservationCancellationException(InvalidReservationCancellationException ex) {
          CustomErrorResponse errorResponse = new CustomErrorResponse(
                  java.time.LocalDateTime.now(),
                  ex.getMessage() 
          );
      
      return GenericResponse.builder()
              .data(errorResponse)
              .status(HttpStatus.CONFLICT)
              .build()
              .buildResponse();
    }
}
