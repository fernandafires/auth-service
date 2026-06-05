package com.fabricadesoftware.authservice.services;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final Key chave;
	private final long expiracaoMs;

	public JwtService(
			@Value("${auth.jwt.secret}") String secret,
			@Value("${auth.jwt.expiration}") long expiracaoMs) {
		this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expiracaoMs = expiracaoMs;
	}

	public String gerarToken(Long professorId, String email, String role) {
		Date agora = new Date();
		Date exp = new Date(agora.getTime() + expiracaoMs);
		return Jwts.builder()
				.setSubject(email)
				.claim("professorId", professorId)
				.claim("role", role)
				.setIssuedAt(agora)
				.setExpiration(exp)
				.signWith(chave, SignatureAlgorithm.HS256)
				.compact();
	}

	public Claims validarToken(String token) {
		String limpo = (token != null && token.startsWith("Bearer ")) ? token.substring(7) : token;
		return Jwts.parserBuilder().setSigningKey(chave).build().parseClaimsJws(limpo).getBody();
	}

	public boolean isValido(String token) {
		try {
			validarToken(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public LocalDateTime expiracaoDe(String token) {
		Date exp = validarToken(token).getExpiration();
		return LocalDateTime.ofInstant(exp.toInstant(), ZoneId.systemDefault());
	}
}
