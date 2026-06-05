package com.fabricadesoftware.authservice.advice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fabricadesoftware.authservice.exceptions.CredencialNaoEncontradaException;
import com.fabricadesoftware.authservice.exceptions.EmailJaCadastradoException;
import com.fabricadesoftware.authservice.exceptions.ProfessorJaCadastradoException;
import com.fabricadesoftware.authservice.exceptions.SenhaIncorretaException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private Map<String, Object> corpo(String erro, String mensagem) {
		Map<String, Object> m = new HashMap<>();
		m.put("erro", erro);
		m.put("mensagem", mensagem);
		m.put("timestamp", LocalDateTime.now());
		return m;
	}

	@ExceptionHandler({ EmailJaCadastradoException.class, ProfessorJaCadastradoException.class })
	public ResponseEntity<Map<String, Object>> conflito(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(corpo("CONFLITO", e.getMessage()));
	}

	@ExceptionHandler(CredencialNaoEncontradaException.class)
	public ResponseEntity<Map<String, Object>> naoEncontrado(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo("NAO_ENCONTRADO", e.getMessage()));
	}

	@ExceptionHandler(SenhaIncorretaException.class)
	public ResponseEntity<Map<String, Object>> naoAutorizado(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(corpo("NAO_AUTORIZADO", e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> validacao(MethodArgumentNotValidException e) {
		String msg = e.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + ": " + f.getDefaultMessage())
				.reduce((a, b) -> a + "; " + b).orElse("Dados invalidos");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo("VALIDACAO", msg));
	}
}
