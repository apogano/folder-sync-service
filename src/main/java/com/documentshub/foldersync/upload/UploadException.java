package com.documentshub.foldersync.upload;

/**
 * Raised for any failed upload attempt. Deliberately a single exception
 * type here -- the scanner's retry policy is uniform: any
 * failed upload gets retried up to the configured max attempts, since from
 * the scanner's point of view it usually can't distinguish "the server
 * rejected this file for good" from "the network hiccuped" without
 * inspecting the response in more detail than is worth doing here. The
 * server itself already makes that permanent/transient distinction on its
 * side of the pipeline.
 */
public class UploadException extends RuntimeException {
    public UploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadException(String message) {
        super(message);
    }
}