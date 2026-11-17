package com.autobots.automanager.hateoas;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ControleServico;
import com.autobots.automanager.entidades.Servico;

@Component
public class ServicoHdr implements AdicionadorLink<Servico> {

	@Override
	public void adicionarLink(List<Servico> lista) {
		for (Servico servico : lista) {
			adicionarLink(servico);
		}
	}

	@Override
	public void adicionarLink(Servico servico) {
		long id = servico.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleServico.class).obterServico(id))
				.withSelfRel();
		servico.add(linkProprio);

		Link linkTodos = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleServico.class).obterServicos())
				.withRel("servicos");
		servico.add(linkTodos);
	}
}

