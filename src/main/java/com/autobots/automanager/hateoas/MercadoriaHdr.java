package com.autobots.automanager.hateoas;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ControleMercadoria;
import com.autobots.automanager.entidades.Mercadoria;

@Component
public class MercadoriaHdr implements AdicionadorLink<Mercadoria> {

	@Override
	public void adicionarLink(List<Mercadoria> lista) {
		for (Mercadoria mercadoria : lista) {
			adicionarLink(mercadoria);
		}
	}

	@Override
	public void adicionarLink(Mercadoria mercadoria) {
		long id = mercadoria.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleMercadoria.class).obterMercadoria(id))
				.withSelfRel();
		mercadoria.add(linkProprio);

		Link linkTodas = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleMercadoria.class).obterMercadorias())
				.withRel("mercadorias");
		mercadoria.add(linkTodas);
	}
}

