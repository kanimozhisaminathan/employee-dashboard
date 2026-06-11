package com.example.demo1.dto;

public record TestMailRequest(
        String host,
        Integer port,
        String username,
        String password,
        String from,
        String appPassword,
        String to
) {
}
