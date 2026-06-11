package com.example.demo1.dto;

public record TestMailResult(
        boolean sent,
        String message
) {
}
