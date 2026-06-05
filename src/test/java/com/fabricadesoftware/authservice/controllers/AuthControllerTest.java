package com.fabricadesoftware.authservice.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fabricadesoftware.authservice.config.SecurityConfig;
import com.fabricadesoftware.authservice.dtos.MensagemResponse;
import com.fabricadesoftware.authservice.dtos.TokenResponse;
import com.fabricadesoftware.authservice.exceptions.SenhaIncorretaException;
import com.fabricadesoftware.authservice.services.CredencialService;
import com.fabricadesoftware.authservice.services.JwtService;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private CredencialService credencialService;
	@MockitoBean
	private JwtService jwtService;

	@Test
	void register_retorna201() throws Exception {
		when(credencialService.register(any()))
				.thenReturn(new MensagemResponse("Credencial criada com sucesso", LocalDateTime.now()));

		mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("{\"professorId\":1,\"email\":\"ana@escola.com\",\"senha\":\"segredo123\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.mensagem").value("Credencial criada com sucesso"));
	}

	@Test
	void login_retornaToken() throws Exception {
		when(credencialService.login(any()))
				.thenReturn(new TokenResponse("token-fake", 1L, "ana@escola.com", "PROFESSOR", LocalDateTime.now()));

		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"ana@escola.com\",\"senha\":\"segredo123\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("token-fake"))
				.andExpect(jsonPath("$.professorId").value(1));
	}

	@Test
	void login_comSenhaErrada_retorna401() throws Exception {
		when(credencialService.login(any())).thenThrow(new SenhaIncorretaException("Senha incorreta"));

		mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"email\":\"ana@escola.com\",\"senha\":\"errada\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void validate_comTokenValido_retornaValidTrue() throws Exception {
		io.jsonwebtoken.Claims claims = org.mockito.Mockito.mock(io.jsonwebtoken.Claims.class);
		when(claims.getSubject()).thenReturn("ana@escola.com");
		when(claims.get("professorId")).thenReturn(1);
		when(claims.get("role")).thenReturn("PROFESSOR");
		when(claims.getExpiration()).thenReturn(new java.util.Date(System.currentTimeMillis() + 3600000));
		when(jwtService.validarToken("tok")).thenReturn(claims);

		mvc.perform(post("/auth/validate").contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"tok\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.valid").value(true))
				.andExpect(jsonPath("$.professorId").value(1));
	}

	@Test
	void validate_comTokenInvalido_retornaValidFalse() throws Exception {
		when(jwtService.validarToken("ruim")).thenThrow(new RuntimeException("invalido"));

		mvc.perform(post("/auth/validate").contentType(MediaType.APPLICATION_JSON)
				.content("{\"token\":\"ruim\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.valid").value(false));
	}
}
