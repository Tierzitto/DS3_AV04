package com.autobots.automanager.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.autobots.automanager.entidades.Usuario;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {

	@Query("SELECT u FROM Usuario u JOIN u.credenciais c WHERE TYPE(c) = com.autobots.automanager.entidades.CredencialUsuarioSenha AND TREAT(c AS com.autobots.automanager.entidades.CredencialUsuarioSenha).nomeUsuario = :nomeUsuario")
	Optional<Usuario> findByNomeUsuario(@Param("nomeUsuario") String nomeUsuario);
}
