package com.autobots.automanager.hateoas;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ControleUsuario;
import com.autobots.automanager.controles.ControleVeiculo;
import com.autobots.automanager.controles.ControleVenda;
import com.autobots.automanager.entidades.Venda;

@Component
public class VendaHdr implements AdicionadorLink<Venda> {

	@Override
	public void adicionarLink(List<Venda> lista) {
		for (Venda venda : lista) {
			adicionarLink(venda);
		}
	}

	@Override
	public void adicionarLink(Venda venda) {
		long id = venda.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleVenda.class).obterVenda(id))
				.withSelfRel();
		venda.add(linkProprio);

		Link linkTodas = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleVenda.class).obterVendas())
				.withRel("vendas");
		venda.add(linkTodas);

		if (venda.getCliente() != null && venda.getCliente().getId() != null) {
			Link linkCliente = WebMvcLinkBuilder
					.linkTo(WebMvcLinkBuilder.methodOn(ControleUsuario.class).obterUsuario(venda.getCliente().getId()))
					.withRel("cliente");
			venda.add(linkCliente);
		}

		if (venda.getFuncionario() != null && venda.getFuncionario().getId() != null) {
			Link linkFuncionario = WebMvcLinkBuilder
					.linkTo(WebMvcLinkBuilder.methodOn(ControleUsuario.class).obterUsuario(venda.getFuncionario().getId()))
					.withRel("funcionario");
			venda.add(linkFuncionario);
		}

		if (venda.getVeiculo() != null && venda.getVeiculo().getId() != null) {
			Link linkVeiculo = WebMvcLinkBuilder
					.linkTo(WebMvcLinkBuilder.methodOn(ControleVeiculo.class).obterVeiculo(venda.getVeiculo().getId()))
					.withRel("veiculo");
			venda.add(linkVeiculo);
		}
	}
}

