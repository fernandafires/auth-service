package com.fabricadesoftware.authservice.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

class JwtServiceTest {

	private JwtService jwtService;
	private final String secret = "segredo-de-teste-com-mais-de-32-bytes-para-hs256-ok";

	@BeforeEach
	void setup() {
		jwtService = new JwtService(secret, 3600000L);
	}

	@Test
	void gerarToken_eDepoisValidar_retornaClaims() {
		String token = jwtService.gerarToken(1L, "ana@escola.com", "PROFESSOR");
		assertNotNull(token);

		Claims claims = jwtService.validarToken(token);
		assertEquals("ana@escola.com", claims.getSubject());
		assertEquals(1, ((Number) claims.get("professorId")).longValue());
		assertEquals("PROFESSOR", claims.get("role"));
	}

	@Test
	void validarToken_comTokenMalformado_lancaExcecao() {
		assertFalse(jwtService.isValido("isto.nao.e.um.token"));
	}

	@Test
	void validarToken_comTokenExpirado_naoEhValido() {
		JwtService curto = new JwtService(secret, -1000L); // ja expirado
		String token = curto.gerarToken(1L, "ana@escola.com", "PROFESSOR");
		assertFalse(curto.isValido(token));
	}

	@Test
	void isValido_comTokenBom_retornaTrue() {
		String token = jwtService.gerarToken(2L, "joao@escola.com", "PROFESSOR");
		assertTrue(jwtService.isValido(token));
	}
}
