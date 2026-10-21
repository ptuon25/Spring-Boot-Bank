package com.tuon.tbank.dto.person;

import java.time.LocalDate;

public record ClientUpdateRequest(String name, LocalDate birthDate, String document, String email) {
}
