package com.autobots.automanager.controles;

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

import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.hateoas.ServicoHdr;
import com.autobots.automanager.repositorios.RepositorioServico;

@RestController
@RequestMapping("/servicos")
public class ControleServico {

	@Autowired
	private RepositorioServico repositorioServico;

	@Autowired
	private ServicoHdr servicoHdr;

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@GetMapping
	public ResponseEntity<List<Servico>> obterServicos() {
		List<Servico> servicos = repositorioServico.findAll();
		if (servicos.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		servicoHdr.adicionarLink(servicos);
		return new ResponseEntity<>(servicos, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Servico> obterServico(@PathVariable Long id) {
		Optional<Servico> servicoOp = repositorioServico.findById(id);
		if (servicoOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Servico servico = servicoOp.get();
		servicoHdr.adicionarLink(servico);
		return new ResponseEntity<>(servico, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@PostMapping
	public ResponseEntity<Servico> cadastrarServico(@RequestBody Servico servico) {
		Servico salvo = repositorioServico.save(servico);
		servicoHdr.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@PutMapping("/{id}")
	public ResponseEntity<Servico> atualizarServico(@PathVariable Long id, @RequestBody Servico atualizacao) {
		Optional<Servico> servicoOp = repositorioServico.findById(id);
		if (servicoOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Servico servico = servicoOp.get();
		if (atualizacao.getNome() != null) {
			servico.setNome(atualizacao.getNome());
		}
		if (atualizacao.getDescricao() != null) {
			servico.setDescricao(atualizacao.getDescricao());
		}
		if (atualizacao.getValor() > 0) {
			servico.setValor(atualizacao.getValor());
		}
		Servico salva = repositorioServico.save(servico);
		servicoHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluirServico(@PathVariable Long id) {
		Optional<Servico> servicoOp = repositorioServico.findById(id);
		if (servicoOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		repositorioServico.delete(servicoOp.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
