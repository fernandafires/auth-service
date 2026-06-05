package com.fabricadesoftware.authservice.exceptions;

public class SenhaIncorretaException extends RuntimeException {
	public SenhaIncorretaException(String mensagem) {
		super(mensagem);
	}
}
