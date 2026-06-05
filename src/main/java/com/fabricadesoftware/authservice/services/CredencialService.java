package com.fabricadesoftware.authservice.services;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fabricadesoftware.authservice.dtos.LoginRequest;
import com.fabricadesoftware.authservice.dtos.MensagemResponse;
import com.fabricadesoftware.authservice.dtos.RegisterRequest;
import com.fabricadesoftware.authservice.dtos.TokenResponse;
import com.fabricadesoftware.authservice.entities.Credencial;
import com.fabricadesoftware.authservice.exceptions.CredencialNaoEncontradaException;
import com.fabricadesoftware.authservice.exceptions.EmailJaCadastradoException;
import com.fabricadesoftware.authservice.exceptions.ProfessorJaCadastradoException;
import com.fabricadesoftware.authservice.exceptions.SenhaIncorretaException;
import com.fabricadesoftware.authservice.repositories.CredencialRepository;

@Service
public class CredencialService {

	private static final String ROLE_PROFESSOR = "PROFESSOR";

	private final CredencialRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public CredencialService(CredencialRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public MensagemResponse register(RegisterRequest req) {
		String email = req.email().toLowerCase();
		if (repository.existsByEmail(email)) {
			throw new EmailJaCadastradoException("Ja existe uma credencial com esse email");
		}
		if (repository.existsByProfessorId(req.professorId())) {
			throw new ProfessorJaCadastradoException("Esse professor ja possui credencial");
		}
		Credencial credencial = new Credencial(
				req.professorId(), email, passwordEncoder.encode(req.senha()), ROLE_PROFESSOR, LocalDateTime.now());
		repository.save(credencial);
		return new MensagemResponse("Credencial criada com sucesso", LocalDateTime.now());
	}

	public TokenResponse login(LoginRequest req) {
		String email = req.email().toLowerCase();
		Credencial c = repository.findByEmail(email)
				.orElseThrow(() -> new CredencialNaoEncontradaException("Credencial nao encontrada"));
		if (!passwordEncoder.matches(req.senha(), c.getSenha())) {
			throw new SenhaIncorretaException("Senha incorreta");
		}
		String token = jwtService.gerarToken(c.getProfessorId(), c.getEmail(), c.getRole());
		LocalDateTime expiraEm = jwtService.expiracaoDe(token);
		return new TokenResponse(token, c.getProfessorId(), c.getEmail(), c.getRole(), expiraEm);
	}
}
