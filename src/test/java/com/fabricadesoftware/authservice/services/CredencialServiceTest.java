package com.fabricadesoftware.authservice.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fabricadesoftware.authservice.dtos.LoginRequest;
import com.fabricadesoftware.authservice.dtos.RegisterRequest;
import com.fabricadesoftware.authservice.dtos.TokenResponse;
import com.fabricadesoftware.authservice.entities.Credencial;
import com.fabricadesoftware.authservice.exceptions.CredencialNaoEncontradaException;
import com.fabricadesoftware.authservice.exceptions.EmailJaCadastradoException;
import com.fabricadesoftware.authservice.exceptions.SenhaIncorretaException;
import com.fabricadesoftware.authservice.repositories.CredencialRepository;

@ExtendWith(MockitoExtension.class)
class CredencialServiceTest {

	@Mock
	private CredencialRepository repository;
	@Mock
	private JwtService jwtService;

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private CredencialService service;

	@BeforeEach
	void setup() {
		service = new CredencialService(repository, passwordEncoder, jwtService);
	}

	@Test
	void register_comDadosNovos_salvaCredencial() {
		when(repository.existsByEmail("ana@escola.com")).thenReturn(false);
		when(repository.existsByProfessorId(1L)).thenReturn(false);

		service.register(new RegisterRequest(1L, "Ana@Escola.com", "segredo123"));

		org.mockito.Mockito.verify(repository).save(any(Credencial.class));
	}

	@Test
	void register_comEmailDuplicado_lancaExcecao() {
		when(repository.existsByEmail("ana@escola.com")).thenReturn(true);
		assertThrows(EmailJaCadastradoException.class,
				() -> service.register(new RegisterRequest(1L, "ana@escola.com", "segredo123")));
	}

	@Test
	void login_comCredenciaisCorretas_retornaToken() {
		String hash = passwordEncoder.encode("segredo123");
		Credencial c = new Credencial(1L, "ana@escola.com", hash, "PROFESSOR", java.time.LocalDateTime.now());
		when(repository.findByEmail("ana@escola.com")).thenReturn(Optional.of(c));
		when(jwtService.gerarToken(1L, "ana@escola.com", "PROFESSOR")).thenReturn("token-fake");
		when(jwtService.expiracaoDe("token-fake")).thenReturn(java.time.LocalDateTime.now().plusHours(1));

		TokenResponse resp = service.login(new LoginRequest("Ana@Escola.com", "segredo123"));

		assertNotNull(resp);
		assertEquals("token-fake", resp.token());
		assertEquals(1L, resp.professorId());
		assertEquals("ana@escola.com", resp.email());
	}

	@Test
	void login_comSenhaErrada_lancaExcecao() {
		String hash = passwordEncoder.encode("certa");
		Credencial c = new Credencial(1L, "ana@escola.com", hash, "PROFESSOR", java.time.LocalDateTime.now());
		when(repository.findByEmail("ana@escola.com")).thenReturn(Optional.of(c));

		assertThrows(SenhaIncorretaException.class,
				() -> service.login(new LoginRequest("ana@escola.com", "errada")));
	}

	@Test
	void login_comEmailInexistente_lancaExcecao() {
		when(repository.findByEmail("nao@existe.com")).thenReturn(Optional.empty());
		assertThrows(CredencialNaoEncontradaException.class,
				() -> service.login(new LoginRequest("nao@existe.com", "qualquer")));
	}
}
