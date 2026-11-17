package com.autobots.automanager.hateoas;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ControleCredencial;
import com.autobots.automanager.entidades.Credencial;

@Component
public class CredencialHdr implements AdicionadorLink<Credencial> {

	@Override
	public void adicionarLink(List<Credencial> lista) {
		for (Credencial credencial : lista) {
			adicionarLink(credencial);
		}
	}

	@Override
	public void adicionarLink(Credencial credencial) {
		long id = credencial.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleCredencial.class).obterCredencial(id))
				.withSelfRel();
		credencial.add(linkProprio);

		Link linkTodas = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleCredencial.class).obterCredenciais())
				.withRel("credenciais");
		credencial.add(linkTodas);
	}
}

