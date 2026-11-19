package com.tuon.tbank.dto.account;

import com.tuon.tbank.entity.person.Client;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CheckingAccountCreateResponse(@Positive Long id,
                                            @NotBlank String accountNumber,
                                            @NotNull @PastOrPresent LocalDate openingDate,
                                            @NotBlank @NotNull Client owner) {
}
