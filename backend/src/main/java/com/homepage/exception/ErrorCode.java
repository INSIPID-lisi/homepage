package com.homepage.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "bad request"),
    NOT_FOUND(404, "not found"),
    INTERNAL_ERROR(500, "internal error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
