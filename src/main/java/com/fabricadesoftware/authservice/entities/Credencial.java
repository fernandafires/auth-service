package com.fabricadesoftware.authservice.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "credenciais")
public class Credencial {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "professor_id", nullable = false, unique = true)
	private Long professorId;

	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@Column(nullable = false, length = 255)
	private String senha;

	@Column(nullable = false, length = 20)
	private String role;

	@Column(name = "data_criacao", nullable = false)
	private LocalDateTime dataCriacao;

	public Credencial() {
	}

	public Credencial(Long professorId, String email, String senha, String role, LocalDateTime dataCriacao) {
		this.professorId = professorId;
		this.email = email;
		this.senha = senha;
		this.role = role;
		this.dataCriacao = dataCriacao;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getProfessorId() { return professorId; }
	public void setProfessorId(Long professorId) { this.professorId = professorId; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getSenha() { return senha; }
	public void setSenha(String senha) { this.senha = senha; }
	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }
	public LocalDateTime getDataCriacao() { return dataCriacao; }
	public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
