package com.autobots.automanager.servicos;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.autobots.automanager.adaptadores.UserDetailsImpl;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;

@Service
public class ServicoAutenticacao {

	public Usuario obterUsuarioAutenticado() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
			return ((UserDetailsImpl) auth.getPrincipal()).getUsuario();
		}
		return null;
	}

	public boolean temPerfil(PerfilUsuario perfil) {
		Usuario usuario = obterUsuarioAutenticado();
		if (usuario == null) {
			return false;
		}
		return usuario.getPerfis().contains(perfil);
	}

	public boolean isAdmin() {
		return temPerfil(PerfilUsuario.ADMINISTRADOR);
	}

	public boolean isGerente() {
		return temPerfil(PerfilUsuario.GERENTE);
	}

	public boolean isVendedor() {
		return temPerfil(PerfilUsuario.VENDEDOR);
	}

	public boolean isCliente() {
		return temPerfil(PerfilUsuario.CLIENTE);
	}
}
