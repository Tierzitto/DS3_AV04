package com.autobots.automanager.hateoas;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.ControleEmpresa;
import com.autobots.automanager.entidades.Empresa;

@Component
public class EmpresaHdr implements AdicionadorLink<Empresa> {

	@Override
	public void adicionarLink(List<Empresa> lista) {
		for (Empresa empresa : lista) {
			adicionarLink(empresa);
		}
	}

	@Override
	public void adicionarLink(Empresa empresa) {
		long id = empresa.getId();
		Link linkProprio = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleEmpresa.class).obterEmpresa(id))
				.withSelfRel();
		empresa.add(linkProprio);

		Link linkTodas = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleEmpresa.class).obterEmpresas())
				.withRel("empresas");
		empresa.add(linkTodas);

		Link linkUsuarios = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleEmpresa.class).obterUsuarios(id))
				.withRel("usuarios");
		empresa.add(linkUsuarios);

		Link linkMercadorias = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleEmpresa.class).obterMercadorias(id))
				.withRel("mercadorias");
		empresa.add(linkMercadorias);

		Link linkServicos = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleEmpresa.class).obterServicos(id))
				.withRel("servicos");
		empresa.add(linkServicos);

		Link linkVendas = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(ControleEmpresa.class).obterVendas(id))
				.withRel("vendas");
		empresa.add(linkVendas);
	}
}

