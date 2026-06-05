package com.fabricadesoftware.authservice.dtos;

import java.time.LocalDateTime;

public record TokenResponse(
		String token,
		Long professorId,
		String email,
		String role,
		LocalDateTime expiraEm) {
}
