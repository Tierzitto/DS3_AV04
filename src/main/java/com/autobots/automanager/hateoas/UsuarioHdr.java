package com.autobots.automanager.hateoas;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ControleUsuario;
import com.autobots.automanager.entidades.Usuario;

@Component
public class UsuarioHdr implements AdicionadorLink<Usuario> {

	@Override
	public void adicionarLink(List<Usuario> lista) {
		for (Usuario usuario : lista) {
			adicionarLink(usuario);
		}
	}

	@Override
	public void adicionarLink(Usuario usuario) {
		long id = usuario.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleUsuario.class).obterUsuario(id))
				.withSelfRel();
		usuario.add(linkProprio);

		Link linkTodos = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleUsuario.class).obterUsuarios())
				.withRel("usuarios");
		usuario.add(linkTodos);

		Link linkCredenciais = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleUsuario.class).obterCredenciais(id))
				.withRel("credenciais");
		usuario.add(linkCredenciais);

		Link linkVeiculos = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleUsuario.class).obterVeiculos(id))
				.withRel("veiculos");
		usuario.add(linkVeiculos);

		Link linkVendas = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleUsuario.class).obterVendas(id))
				.withRel("vendas");
		usuario.add(linkVendas);
	}
}

