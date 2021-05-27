/*-
 * ---license-start
 * EU Digital Green Certificate Issuance Service / dgca-issuance-service
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package eu.europa.ec.dgc.issuance.config;

import eu.europa.ec.dgc.issuance.restapi.dto.ProblemReportDto;
import eu.europa.ec.dgc.issuance.service.DdcGatewayException;
import eu.europa.ec.dgc.issuance.service.DgciConflict;
import eu.europa.ec.dgc.issuance.service.DgciNotFound;
import eu.europa.ec.dgc.issuance.service.WrongRequest;
import javax.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ErrorHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles {@link ConstraintViolationException} when a validation failed.
     *
     * @param e the thrown {@link ConstraintViolationException}
     * @return A ResponseEntity with a ErrorMessage inside.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemReportDto> handleException(ConstraintViolationException e) {
        log.error(e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ProblemReportDto("", "Validation Error", "", e.getMessage()));
    }

    /**
     * Exception Handler to handle {@link DgciNotFound} Exceptions.
     */
    @ExceptionHandler(DgciNotFound.class)
    public ResponseEntity<ProblemReportDto> handleException(DgciNotFound e) {
        log.error(e.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ProblemReportDto("", "DGCI not found", "", e.getMessage()));
    }

    /**
     * Exception Handler to handle {@link DgciConflict} Exceptions.
     */
    @ExceptionHandler(DgciConflict.class)
    public ResponseEntity<ProblemReportDto> handleException(DgciConflict e) {
        log.error(e.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ProblemReportDto("", "DGCI conflict", "", e.getMessage()));
    }

    /**
     * Exception Handler to handle {@link DdcGatewayException} Exceptions.
     */
    @ExceptionHandler(DdcGatewayException.class)
    public ResponseEntity<ProblemReportDto> handleException(DdcGatewayException e) {
        log.error(e.getMessage());
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ProblemReportDto("", "DGCGataway error: " + e.getMessage(),
                "", e.getCause() != null ? e.getCause().getMessage() : null));
    }

    /**
     * Exception Handler to handle {@link WrongRequest} Exceptions.
     */
    @ExceptionHandler(WrongRequest.class)
    public ResponseEntity<ProblemReportDto> handleException(WrongRequest e) {
        log.error(e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ProblemReportDto("", "Wrong request", "", e.getMessage()));
    }

    /**
     * Global Exception Handler to wrap exceptions into a readable JSON Object.
     *
     * @param e the thrown exception
     * @return ResponseEntity with readable data.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemReportDto> handleException(Exception e) {
        if (e instanceof ResponseStatusException) {
            return ResponseEntity
                .status(((ResponseStatusException) e).getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ProblemReportDto("co", "prob", "val", "det"));
        } else {
            log.error("Uncatched exception", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ProblemReportDto("0x500", "Internal Server Error", "", ""));
        }
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();

            sb
                .append(error.getObjectName())
                .append('.')
                .append(fieldName)
                .append(' ')
                .append(errorMessage)
                .append(", ");
        });
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new ProblemReportDto("", "Validation Error", "", sb.toString()));
    }
}
