package com.driverkonnect.backend.generics;

public record Response<T>(T data, String message, int status) {

    public Response(int status) {
        this(null, null, status);
    }

    public Response(T data, int status) {
        this(data, null, status);
    }

    public Response(String message, int status) {
        this(null, message, status);
    }
}
