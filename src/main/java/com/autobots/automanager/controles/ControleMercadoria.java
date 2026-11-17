package com.autobots.automanager.controles;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.hateoas.MercadoriaHdr;
import com.autobots.automanager.repositorios.RepositorioMercadoria;

@RestController
@RequestMapping("/mercadorias")
public class ControleMercadoria {

	@Autowired
	private RepositorioMercadoria repositorioMercadoria;

	@Autowired
	private MercadoriaHdr mercadoriaHdr;

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@GetMapping
	public ResponseEntity<List<Mercadoria>> obterMercadorias() {
		List<Mercadoria> mercadorias = repositorioMercadoria.findAll();
		if (mercadorias.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		mercadoriaHdr.adicionarLink(mercadorias);
		return new ResponseEntity<>(mercadorias, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Mercadoria> obterMercadoria(@PathVariable Long id) {
		Optional<Mercadoria> mercadoriaOp = repositorioMercadoria.findById(id);
		if (mercadoriaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Mercadoria mercadoria = mercadoriaOp.get();
		mercadoriaHdr.adicionarLink(mercadoria);
		return new ResponseEntity<>(mercadoria, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@PostMapping
	public ResponseEntity<Mercadoria> cadastrarMercadoria(@RequestBody Mercadoria mercadoria) {
		if (mercadoria.getCadastro() == null) {
			mercadoria.setCadastro(new Date());
		}
		Mercadoria salva = repositorioMercadoria.save(mercadoria);
		mercadoriaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.CREATED);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@PutMapping("/{id}")
	public ResponseEntity<Mercadoria> atualizarMercadoria(@PathVariable Long id, @RequestBody Mercadoria atualizacao) {
		Optional<Mercadoria> mercadoriaOp = repositorioMercadoria.findById(id);
		if (mercadoriaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Mercadoria mercadoria = mercadoriaOp.get();
		if (atualizacao.getNome() != null) {
			mercadoria.setNome(atualizacao.getNome());
		}
		if (atualizacao.getDescricao() != null) {
			mercadoria.setDescricao(atualizacao.getDescricao());
		}
		if (atualizacao.getQuantidade() > 0) {
			mercadoria.setQuantidade(atualizacao.getQuantidade());
		}
		if (atualizacao.getValor() > 0) {
			mercadoria.setValor(atualizacao.getValor());
		}
		if (atualizacao.getFabricao() != null) {
			mercadoria.setFabricao(atualizacao.getFabricao());
		}
		if (atualizacao.getValidade() != null) {
			mercadoria.setValidade(atualizacao.getValidade());
		}
		Mercadoria salva = repositorioMercadoria.save(mercadoria);
		mercadoriaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluirMercadoria(@PathVariable Long id) {
		Optional<Mercadoria> mercadoriaOp = repositorioMercadoria.findById(id);
		if (mercadoriaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		repositorioMercadoria.delete(mercadoriaOp.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
