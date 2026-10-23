package com.tuon.tbank.exception.controllerException;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.Instant;

/**
 * Represents the standard error details returned by the API.
 */
@Getter
@Setter
public class StandardError {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "GMT")
    private Instant timestamp;

    private Integer status;
    private String error;
    private String message;
    private String path;

    /**
     * Creates an error response with no fields initialized.
     */
    public StandardError(){ }

    /**
     * Creates an error response with all fields initialized.
     *
     * @param timestamp the time at which the error occurred
     * @param status the HTTP status code
     * @param error the error description
     * @param message the error detail message
     * @param path the request URI
     */
    public StandardError(Instant timestamp, Integer status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }


}
