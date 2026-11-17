package com.autobots.automanager.controles;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.hateoas.VeiculoHdr;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVeiculo;
import com.autobots.automanager.servicos.ServicoAutenticacao;

@RestController
@RequestMapping("/veiculos")
public class ControleVeiculo {

	@Autowired
	private RepositorioVeiculo repositorioVeiculo;

	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Autowired
	private VeiculoHdr veiculoHdr;

	@Autowired
	private ServicoAutenticacao servicoAuth;

	private boolean podeAcessarVeiculo(Veiculo veiculo) {
		Usuario autenticado = servicoAuth.obterUsuarioAutenticado();
		if (autenticado == null) {
			return false;
		}
		if (servicoAuth.isAdmin() || servicoAuth.isGerente() || servicoAuth.isVendedor()) {
			return true;
		}
		if (servicoAuth.isCliente()) {
			return veiculo.getProprietario() != null && Objects.equals(veiculo.getProprietario().getId(), autenticado.getId());
		}
		return false;
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping
	public ResponseEntity<List<Veiculo>> obterVeiculos() {
		Usuario autenticado = servicoAuth.obterUsuarioAutenticado();
		if (autenticado == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		List<Veiculo> todos = repositorioVeiculo.findAll();
		List<Veiculo> filtrados;

		if (servicoAuth.isCliente()) {
			filtrados = todos.stream()
					.filter(v -> v.getProprietario() != null && Objects.equals(v.getProprietario().getId(), autenticado.getId()))
					.collect(Collectors.toList());
		} else {
			filtrados = todos;
		}

		if (filtrados.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		veiculoHdr.adicionarLink(filtrados);
		return new ResponseEntity<>(filtrados, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/{id}")
	public ResponseEntity<Veiculo> obterVeiculo(@PathVariable Long id) {
		Optional<Veiculo> veiculoOp = repositorioVeiculo.findById(id);
		if (veiculoOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Veiculo veiculo = veiculoOp.get();
		if (!podeAcessarVeiculo(veiculo)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		veiculoHdr.adicionarLink(veiculo);
		return new ResponseEntity<>(veiculo, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@PostMapping
	public ResponseEntity<Veiculo> cadastrarVeiculo(@RequestBody Veiculo veiculo) {
		if (veiculo.getProprietario() != null && veiculo.getProprietario().getId() != null) {
			repositorioUsuario.findById(veiculo.getProprietario().getId()).ifPresent(veiculo::setProprietario);
		}
		Veiculo salvo = repositorioVeiculo.save(veiculo);
		veiculoHdr.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@PutMapping("/{id}")
	public ResponseEntity<Veiculo> atualizarVeiculo(@PathVariable Long id, @RequestBody Veiculo atualizacao) {
		Optional<Veiculo> veiculoOp = repositorioVeiculo.findById(id);
		if (veiculoOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Veiculo veiculo = veiculoOp.get();
		if (atualizacao.getPlaca() != null) {
			veiculo.setPlaca(atualizacao.getPlaca());
		}
		if (atualizacao.getModelo() != null) {
			veiculo.setModelo(atualizacao.getModelo());
		}
		if (atualizacao.getTipo() != null) {
			veiculo.setTipo(atualizacao.getTipo());
		}
		if (atualizacao.getProprietario() != null && atualizacao.getProprietario().getId() != null) {
			repositorioUsuario.findById(atualizacao.getProprietario().getId()).ifPresent(veiculo::setProprietario);
		}
		Veiculo salvo = repositorioVeiculo.save(veiculo);
		veiculoHdr.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluirVeiculo(@PathVariable Long id) {
		Optional<Veiculo> veiculoOp = repositorioVeiculo.findById(id);
		if (veiculoOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		repositorioVeiculo.delete(veiculoOp.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/{id}/proprietario/{idProprietario}")
	public ResponseEntity<Veiculo> associarProprietario(@PathVariable Long id, @PathVariable Long idProprietario) {
		Optional<Veiculo> veiculoOp = repositorioVeiculo.findById(id);
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(idProprietario);
		if (veiculoOp.isEmpty() || usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Veiculo veiculo = veiculoOp.get();
		veiculo.setProprietario(usuarioOp.get());
		Veiculo salvo = repositorioVeiculo.save(veiculo);
		veiculoHdr.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}
}
