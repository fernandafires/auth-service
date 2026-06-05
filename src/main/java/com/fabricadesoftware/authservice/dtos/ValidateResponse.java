package com.fabricadesoftware.authservice.dtos;

import java.time.LocalDateTime;

public record ValidateResponse(
		boolean valid,
		Long professorId,
		String email,
		String role,
		LocalDateTime expiraEm,
		String motivo) {

	public static ValidateResponse ok(Long professorId, String email, String role, LocalDateTime expiraEm) {
		return new ValidateResponse(true, professorId, email, role, expiraEm, null);
	}

	public static ValidateResponse invalido(String motivo) {
		return new ValidateResponse(false, null, null, null, null, motivo);
	}
}
