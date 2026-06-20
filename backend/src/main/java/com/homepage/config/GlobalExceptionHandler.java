package com.homepage.config;

import com.homepage.dto.Result;
import com.homepage.exception.BusinessException;
import com.homepage.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletResponse response) {
        response.setStatus(e.getCode());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e, HttpServletResponse response) {
        response.setStatus(400);
        return Result.error(400, "file too large, max 10MB");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletResponse response) {
        response.setStatus(500);
        return Result.error(ErrorCode.INTERNAL_ERROR);
    }
}
