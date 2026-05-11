package com.simul.auth.application.dto;

public record EmailLoginCommand(
    String email,
    String password
) {}
