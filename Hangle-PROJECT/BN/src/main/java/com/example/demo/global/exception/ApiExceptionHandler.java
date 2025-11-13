package com.example.demo.global.exception;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorBody handleNotFound(ResourceNotFoundException e) {
        return new ErrorBody(404, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorBody handleConflict(ConflictException e) {
        return new ErrorBody(409, e.getMessage());
    }

    // ✅ 추가된 부분 — DB의 UNIQUE 제약 등 위반 시 잡아줌
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorBody handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String msg = "이미 존재하는 데이터입니다. (중복 값 또는 무결성 제약 위반)";
        return new ErrorBody(409, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorBody handleBadRequest(IllegalArgumentException e) {
        return new ErrorBody(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorBody handleEtc(Exception e) {
        return new ErrorBody(500, "Internal Server Error");
    }

    public record ErrorBody(int status, String message) {}
}
