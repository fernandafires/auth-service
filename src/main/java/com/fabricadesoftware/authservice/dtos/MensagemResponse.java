package com.fabricadesoftware.authservice.dtos;

import java.time.LocalDateTime;

public record MensagemResponse(String mensagem, LocalDateTime timestamp) {
}
