package com.fabricadesoftware.authservice.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fabricadesoftware.authservice.dtos.LoginRequest;
import com.fabricadesoftware.authservice.dtos.MensagemResponse;
import com.fabricadesoftware.authservice.dtos.RegisterRequest;
import com.fabricadesoftware.authservice.dtos.TokenResponse;
import com.fabricadesoftware.authservice.dtos.ValidateRequest;
import com.fabricadesoftware.authservice.dtos.ValidateResponse;
import com.fabricadesoftware.authservice.services.CredencialService;
import com.fabricadesoftware.authservice.services.JwtService;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final CredencialService credencialService;
	private final JwtService jwtService;

	public AuthController(CredencialService credencialService, JwtService jwtService) {
		this.credencialService = credencialService;
		this.jwtService = jwtService;
	}

	@PostMapping("/register")
	public ResponseEntity<MensagemResponse> register(@Valid @RequestBody RegisterRequest req) {
		return ResponseEntity.status(HttpStatus.CREATED).body(credencialService.register(req));
	}

	@PostMapping("/login")
	public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
		return ResponseEntity.ok(credencialService.login(req));
	}

	@PostMapping("/validate")
	public ResponseEntity<ValidateResponse> validate(@RequestBody ValidateRequest req) {
		try {
			Claims claims = jwtService.validarToken(req.token());
			Long professorId = ((Number) claims.get("professorId")).longValue();
			String role = (String) claims.get("role");
			Date exp = claims.getExpiration();
			LocalDateTime expiraEm = LocalDateTime.ofInstant(exp.toInstant(), ZoneId.systemDefault());
			return ResponseEntity.ok(ValidateResponse.ok(professorId, claims.getSubject(), role, expiraEm));
		} catch (Exception e) {
			return ResponseEntity.ok(ValidateResponse.invalido("Token invalido ou expirado"));
		}
	}
}
