package com.autobots.automanager.hateoas;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ControleVeiculo;
import com.autobots.automanager.controles.ControleUsuario;
import com.autobots.automanager.entidades.Veiculo;

@Component
public class VeiculoHdr implements AdicionadorLink<Veiculo> {

	@Override
	public void adicionarLink(List<Veiculo> lista) {
		for (Veiculo veiculo : lista) {
			adicionarLink(veiculo);
		}
	}

	@Override
	public void adicionarLink(Veiculo veiculo) {
		long id = veiculo.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleVeiculo.class).obterVeiculo(id))
				.withSelfRel();
		veiculo.add(linkProprio);

		Link linkTodos = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleVeiculo.class).obterVeiculos())
				.withRel("veiculos");
		veiculo.add(linkTodos);

		if (veiculo.getProprietario() != null && veiculo.getProprietario().getId() != null) {
			Link linkProprietario = WebMvcLinkBuilder
					.linkTo(WebMvcLinkBuilder.methodOn(ControleUsuario.class).obterUsuario(veiculo.getProprietario().getId()))
					.withRel("proprietario");
			veiculo.add(linkProprietario);
		}
	}
}

