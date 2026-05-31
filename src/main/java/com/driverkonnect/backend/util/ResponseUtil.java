package com.driverkonnect.backend.util;

import com.driverkonnect.backend.generics.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

    public static <T> ResponseEntity<Response<T>> success(T data, String message) {
        return ResponseEntity.ok(new Response<>(data, message, HttpStatus.OK.value()));
    }

    public static <T> ResponseEntity<Response<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new Response<>(data, message, HttpStatus.CREATED.value()));
    }
}
