package com.autobots.automanager.adaptadores;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private RepositorioUsuario repositorio;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<Usuario> usuarioOp = repositorio.findByNomeUsuario(username);
		if (usuarioOp.isEmpty()) {
			// Fallback: busca iterativa caso seja necessário
			List<Usuario> usuarios = repositorio.findAll();
			for (Usuario u : usuarios) {
				for (Credencial c : u.getCredenciais()) {
					if (c instanceof CredencialUsuarioSenha) {
						CredencialUsuarioSenha cus = (CredencialUsuarioSenha) c;
						if (cus.getNomeUsuario().equals(username)) {
							return new UserDetailsImpl(u, cus);
						}
					}
				}
			}
			throw new UsernameNotFoundException("Usuário não encontrado: " + username);
		}

		Usuario usuario = usuarioOp.get();
		for (Credencial c : usuario.getCredenciais()) {
			if (c instanceof CredencialUsuarioSenha) {
				CredencialUsuarioSenha cus = (CredencialUsuarioSenha) c;
				if (cus.getNomeUsuario().equals(username)) {
					return new UserDetailsImpl(usuario, cus);
				}
			}
		}

		throw new UsernameNotFoundException("Credencial do usuário não encontrada: " + username);
	}
}
