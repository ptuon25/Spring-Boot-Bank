package com.tuon.tbank.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClientRegistrationRequest(
        @NotBlank String name,
        @NotNull @PastOrPresent LocalDate birthDate,
        @NotBlank String document,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 12, max = 72) String password
) {
}
