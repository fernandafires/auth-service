package com.fabricadesoftware.authservice.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fabricadesoftware.authservice.entities.Credencial;

public interface CredencialRepository extends JpaRepository<Credencial, Long> {
	Optional<Credencial> findByEmail(String email);
	boolean existsByEmail(String email);
	boolean existsByProfessorId(Long professorId);
}
