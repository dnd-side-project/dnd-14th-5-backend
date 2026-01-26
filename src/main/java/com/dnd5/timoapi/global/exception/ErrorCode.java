package com.dnd5.timoapi.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

    /**
 * HTTP status to use for this error code.
 *
 * @return the HTTP status associated with this error
 */
HttpStatus getStatus();
    /**
 * Provides the machine-readable error code identifying this error.
 *
 * @return the unique machine-readable error code for this error
 */
String getCode();
    /**
 * Retrieve the human-readable message associated with this error code.
 *
 * @return the error message suitable for display to end users
 */
String getMessage();
}