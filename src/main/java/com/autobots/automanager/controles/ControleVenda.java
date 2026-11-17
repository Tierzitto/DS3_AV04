package com.autobots.automanager.controles;

import java.util.Date;
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

import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.hateoas.VendaHdr;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioServico;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVeiculo;
import com.autobots.automanager.repositorios.RepositorioVenda;
import com.autobots.automanager.servicos.ServicoAutenticacao;

@RestController
@RequestMapping("/vendas")
public class ControleVenda {

	@Autowired
	private RepositorioVenda repositorioVenda;

	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Autowired
	private RepositorioVeiculo repositorioVeiculo;

	@Autowired
	private RepositorioMercadoria repositorioMercadoria;

	@Autowired
	private RepositorioServico repositorioServico;

	@Autowired
	private VendaHdr vendaHdr;

	@Autowired
	private ServicoAutenticacao servicoAuth;

	private boolean podeAcessarVenda(Venda venda) {
		Usuario autenticado = servicoAuth.obterUsuarioAutenticado();
		if (autenticado == null) {
			return false;
		}
		if (servicoAuth.isAdmin() || servicoAuth.isGerente()) {
			return true;
		}
		if (servicoAuth.isVendedor()) {
			return venda.getFuncionario() != null && Objects.equals(venda.getFuncionario().getId(), autenticado.getId());
		}
		if (servicoAuth.isCliente()) {
			return venda.getCliente() != null && Objects.equals(venda.getCliente().getId(), autenticado.getId());
		}
		return false;
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping
	public ResponseEntity<List<Venda>> obterVendas() {
		Usuario autenticado = servicoAuth.obterUsuarioAutenticado();
		if (autenticado == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		List<Venda> todas = repositorioVenda.findAll();
		List<Venda> filtradas;

		if (servicoAuth.isAdmin() || servicoAuth.isGerente()) {
			filtradas = todas;
		} else if (servicoAuth.isVendedor()) {
			filtradas = todas.stream()
					.filter(v -> v.getFuncionario() != null && Objects.equals(v.getFuncionario().getId(), autenticado.getId()))
					.collect(Collectors.toList());
		} else {
			filtradas = todas.stream()
					.filter(v -> v.getCliente() != null && Objects.equals(v.getCliente().getId(), autenticado.getId()))
					.collect(Collectors.toList());
		}

		if (filtradas.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		vendaHdr.adicionarLink(filtradas);
		return new ResponseEntity<>(filtradas, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/{id}")
	public ResponseEntity<Venda> obterVenda(@PathVariable Long id) {
		Optional<Venda> vendaOp = repositorioVenda.findById(id);
		if (vendaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Venda venda = vendaOp.get();
		if (!podeAcessarVenda(venda)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		vendaHdr.adicionarLink(venda);
		return new ResponseEntity<>(venda, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@PostMapping
	public ResponseEntity<?> cadastrarVenda(@RequestBody Venda venda) {
		Usuario autenticado = servicoAuth.obterUsuarioAutenticado();
		if (autenticado == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		if (venda.getCadastro() == null) {
			venda.setCadastro(new Date());
		}

		// Na criação de venda, se for vendedor, o vendedor VEM DO TOKEN
		if (servicoAuth.isVendedor()) {
			venda.setFuncionario(autenticado);
		} else {
			// Admin ou Gerente podem definir ou herdam o próprio usuário se omitido
			if (venda.getFuncionario() != null && venda.getFuncionario().getId() != null) {
				repositorioUsuario.findById(venda.getFuncionario().getId()).ifPresent(venda::setFuncionario);
			} else {
				venda.setFuncionario(autenticado);
			}
		}

		if (venda.getCliente() != null && venda.getCliente().getId() != null) {
			repositorioUsuario.findById(venda.getCliente().getId()).ifPresent(venda::setCliente);
		}
		if (venda.getVeiculo() != null && venda.getVeiculo().getId() != null) {
			repositorioVeiculo.findById(venda.getVeiculo().getId()).ifPresent(venda::setVeiculo);
		}

		Venda salva = repositorioVenda.save(venda);
		vendaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.CREATED);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@PutMapping("/{id}")
	public ResponseEntity<Venda> atualizarVenda(@PathVariable Long id, @RequestBody Venda atualizacao) {
		Optional<Venda> vendaOp = repositorioVenda.findById(id);
		if (vendaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Venda venda = vendaOp.get();
		if (atualizacao.getIdentificacao() != null) {
			venda.setIdentificacao(atualizacao.getIdentificacao());
		}
		if (atualizacao.getCliente() != null && atualizacao.getCliente().getId() != null) {
			repositorioUsuario.findById(atualizacao.getCliente().getId()).ifPresent(venda::setCliente);
		}
		if (atualizacao.getFuncionario() != null && atualizacao.getFuncionario().getId() != null) {
			repositorioUsuario.findById(atualizacao.getFuncionario().getId()).ifPresent(venda::setFuncionario);
		}
		if (atualizacao.getVeiculo() != null && atualizacao.getVeiculo().getId() != null) {
			repositorioVeiculo.findById(atualizacao.getVeiculo().getId()).ifPresent(venda::setVeiculo);
		}
		Venda salva = repositorioVenda.save(venda);
		vendaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluirVenda(@PathVariable Long id) {
		Optional<Venda> vendaOp = repositorioVenda.findById(id);
		if (vendaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		repositorioVenda.delete(vendaOp.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@PostMapping("/{id}/cliente/{idCliente}")
	public ResponseEntity<Venda> associarCliente(@PathVariable Long id, @PathVariable Long idCliente) {
		Optional<Venda> vendaOp = repositorioVenda.findById(id);
		Optional<Usuario> clienteOp = repositorioUsuario.findById(idCliente);
		if (vendaOp.isEmpty() || clienteOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Venda venda = vendaOp.get();
		venda.setCliente(clienteOp.get());
		Venda salva = repositorioVenda.save(venda);
		vendaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@PostMapping("/{id}/funcionario/{idFuncionario}")
	public ResponseEntity<Venda> associarFuncionario(@PathVariable Long id, @PathVariable Long idFuncionario) {
		Optional<Venda> vendaOp = repositorioVenda.findById(id);
		Optional<Usuario> funcOp = repositorioUsuario.findById(idFuncionario);
		if (vendaOp.isEmpty() || funcOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Venda venda = vendaOp.get();
		venda.setFuncionario(funcOp.get());
		Venda salva = repositorioVenda.save(venda);
		vendaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@PostMapping("/{id}/veiculo/{idVeiculo}")
	public ResponseEntity<Venda> associarVeiculo(@PathVariable Long id, @PathVariable Long idVeiculo) {
		Optional<Venda> vendaOp = repositorioVenda.findById(id);
		Optional<Veiculo> veiculoOp = repositorioVeiculo.findById(idVeiculo);
		if (vendaOp.isEmpty() || veiculoOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Venda venda = vendaOp.get();
		venda.setVeiculo(veiculoOp.get());
		Venda salva = repositorioVenda.save(venda);
		vendaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@PostMapping("/{id}/mercadoria/{idMercadoria}")
	public ResponseEntity<Venda> associarMercadoria(@PathVariable Long id, @PathVariable Long idMercadoria) {
		Optional<Venda> vendaOp = repositorioVenda.findById(id);
		Optional<Mercadoria> mercadoriaOp = repositorioMercadoria.findById(idMercadoria);
		if (vendaOp.isEmpty() || mercadoriaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Venda venda = vendaOp.get();
		venda.getMercadorias().add(mercadoriaOp.get());
		Venda salva = repositorioVenda.save(venda);
		vendaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@PostMapping("/{id}/servico/{idServico}")
	public ResponseEntity<Venda> associarServico(@PathVariable Long id, @PathVariable Long idServico) {
		Optional<Venda> vendaOp = repositorioVenda.findById(id);
		Optional<Servico> servicoOp = repositorioServico.findById(idServico);
		if (vendaOp.isEmpty() || servicoOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Venda venda = vendaOp.get();
		venda.getServicos().add(servicoOp.get());
		Venda salva = repositorioVenda.save(venda);
		vendaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}
}
