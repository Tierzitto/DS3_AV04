package com.autobots.automanager.controles;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.hateoas.CredencialHdr;
import com.autobots.automanager.hateoas.UsuarioHdr;
import com.autobots.automanager.hateoas.VeiculoHdr;
import com.autobots.automanager.hateoas.VendaHdr;
import com.autobots.automanager.repositorios.RepositorioCredencial;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVeiculo;
import com.autobots.automanager.servicos.ServicoAutenticacao;

@RestController
@RequestMapping("/usuarios")
public class ControleUsuario {

	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Autowired
	private RepositorioCredencial repositorioCredencial;

	@Autowired
	private RepositorioVeiculo repositorioVeiculo;

	@Autowired
	private UsuarioHdr usuarioHdr;

	@Autowired
	private CredencialHdr credencialHdr;

	@Autowired
	private VeiculoHdr veiculoHdr;

	@Autowired
	private VendaHdr vendaHdr;

	@Autowired
	private ServicoAutenticacao servicoAuth;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private boolean podeAcessarUsuario(Usuario alvo) {
		Usuario autenticado = servicoAuth.obterUsuarioAutenticado();
		if (autenticado == null) {
			return false;
		}
		if (servicoAuth.isAdmin()) {
			return true;
		}
		if (servicoAuth.isGerente()) {
			// Gerente não pode acessar/mexer em administrador
			return !alvo.getPerfis().contains(PerfilUsuario.ADMINISTRADOR);
		}
		if (servicoAuth.isVendedor()) {
			// Vendedor só mexe em clientes ou em si próprio (leitura)
			return Objects.equals(autenticado.getId(), alvo.getId()) || alvo.getPerfis().contains(PerfilUsuario.CLIENTE);
		}
		if (servicoAuth.isCliente()) {
			// Cliente só lê o próprio cadastro
			return Objects.equals(autenticado.getId(), alvo.getId());
		}
		return false;
	}

	private boolean podeModificarUsuario(Usuario alvo) {
		Usuario autenticado = servicoAuth.obterUsuarioAutenticado();
		if (autenticado == null) {
			return false;
		}
		if (servicoAuth.isAdmin()) {
			return true;
		}
		if (servicoAuth.isGerente()) {
			// Gerente não mexe em administradores
			return !alvo.getPerfis().contains(PerfilUsuario.ADMINISTRADOR);
		}
		if (servicoAuth.isVendedor()) {
			// Vendedor só mexe em clientes
			return alvo.getPerfis().size() == 1 && alvo.getPerfis().contains(PerfilUsuario.CLIENTE);
		}
		return false;
	}

	private boolean perfilPermitidoParaCriacao(Set<PerfilUsuario> perfis) {
		if (servicoAuth.isAdmin()) {
			return true;
		}
		if (perfis == null || perfis.isEmpty()) {
			return true;
		}
		if (servicoAuth.isGerente()) {
			// Não pode criar administrador
			return !perfis.contains(PerfilUsuario.ADMINISTRADOR);
		}
		if (servicoAuth.isVendedor()) {
			// Vendedor só cria clientes
			return perfis.stream().allMatch(p -> p == PerfilUsuario.CLIENTE);
		}
		return false;
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping
	public ResponseEntity<List<Usuario>> obterUsuarios() {
		Usuario autenticado = servicoAuth.obterUsuarioAutenticado();
		if (autenticado == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		List<Usuario> todos = repositorioUsuario.findAll();
		List<Usuario> filtrados;

		if (servicoAuth.isAdmin()) {
			filtrados = todos;
		} else if (servicoAuth.isGerente()) {
			// Gerente vê gerente, vendedor e cliente
			filtrados = todos.stream()
					.filter(u -> !u.getPerfis().contains(PerfilUsuario.ADMINISTRADOR))
					.collect(Collectors.toList());
		} else if (servicoAuth.isVendedor()) {
			// Vendedor vê clientes e ele próprio
			filtrados = todos.stream()
					.filter(u -> u.getPerfis().contains(PerfilUsuario.CLIENTE) || Objects.equals(u.getId(), autenticado.getId()))
					.collect(Collectors.toList());
		} else {
			// Cliente só vê o próprio cadastro
			filtrados = todos.stream()
					.filter(u -> Objects.equals(u.getId(), autenticado.getId()))
					.collect(Collectors.toList());
		}

		if (filtrados.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		usuarioHdr.adicionarLink(filtrados);
		return new ResponseEntity<>(filtrados, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/{id}")
	public ResponseEntity<Usuario> obterUsuario(@PathVariable Long id) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = usuarioOp.get();
		if (!podeAcessarUsuario(usuario)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		usuarioHdr.adicionarLink(usuario);
		return new ResponseEntity<>(usuario, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@PostMapping
	public ResponseEntity<?> cadastrarUsuario(@RequestBody Usuario usuario) {
		if (!perfilPermitidoParaCriacao(usuario.getPerfis())) {
			return new ResponseEntity<>("Perfil não permitido para o usuário solicitante.", HttpStatus.FORBIDDEN);
		}

		// Codifica senha de credenciais caso venham no cadastro
		if (usuario.getCredenciais() != null) {
			for (Credencial c : usuario.getCredenciais()) {
				if (c instanceof CredencialUsuarioSenha) {
					CredencialUsuarioSenha cus = (CredencialUsuarioSenha) c;
					if (cus.getSenha() != null && !cus.getSenha().startsWith("$2a$")) {
						cus.setSenha(passwordEncoder.encode(cus.getSenha()));
					}
				}
			}
		}

		Usuario salvo = repositorioUsuario.save(usuario);
		usuarioHdr.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.CREATED);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@PutMapping("/{id}")
	public ResponseEntity<?> atualizarUsuario(@PathVariable Long id, @RequestBody Usuario atualizacao) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = usuarioOp.get();
		Usuario autenticado = servicoAuth.obterUsuarioAutenticado();

		if (!podeModificarUsuario(usuario)) {
			return new ResponseEntity<>("Sem permissão para modificar este usuário.", HttpStatus.FORBIDDEN);
		}

		// Ninguém muda o próprio perfil
		if (Objects.equals(autenticado.getId(), usuario.getId()) && atualizacao.getPerfis() != null
				&& !atualizacao.getPerfis().isEmpty() && !usuario.getPerfis().equals(atualizacao.getPerfis())) {
			return new ResponseEntity<>("Não é permitido alterar o próprio perfil.", HttpStatus.FORBIDDEN);
		}

		// Ninguém cria ou eleva usuário para perfil maior que o seu
		if (atualizacao.getPerfis() != null && !atualizacao.getPerfis().isEmpty()) {
			if (!perfilPermitidoParaCriacao(atualizacao.getPerfis())) {
				return new ResponseEntity<>("Perfil especificado não permitido.", HttpStatus.FORBIDDEN);
			}
			usuario.getPerfis().clear();
			usuario.getPerfis().addAll(atualizacao.getPerfis());
		}

		if (atualizacao.getNome() != null) {
			usuario.setNome(atualizacao.getNome());
		}
		if (atualizacao.getNomeSocial() != null) {
			usuario.setNomeSocial(atualizacao.getNomeSocial());
		}
		if (atualizacao.getEndereco() != null) {
			usuario.setEndereco(atualizacao.getEndereco());
		}
		if (atualizacao.getTelefones() != null && !atualizacao.getTelefones().isEmpty()) {
			usuario.getTelefones().clear();
			usuario.getTelefones().addAll(atualizacao.getTelefones());
		}
		if (atualizacao.getEmails() != null && !atualizacao.getEmails().isEmpty()) {
			usuario.getEmails().clear();
			usuario.getEmails().addAll(atualizacao.getEmails());
		}
		if (atualizacao.getDocumentos() != null && !atualizacao.getDocumentos().isEmpty()) {
			usuario.getDocumentos().clear();
			usuario.getDocumentos().addAll(atualizacao.getDocumentos());
		}

		Usuario atualizado = repositorioUsuario.save(usuario);
		usuarioHdr.adicionarLink(atualizado);
		return new ResponseEntity<>(atualizado, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@DeleteMapping("/{id}")
	public ResponseEntity<?> excluirUsuario(@PathVariable Long id) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = usuarioOp.get();
		if (!podeModificarUsuario(usuario)) {
			return new ResponseEntity<>("Sem permissão para excluir este usuário.", HttpStatus.FORBIDDEN);
		}
		repositorioUsuario.delete(usuario);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	// Sub-rotas de telefones, emails, documentos e endereco
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/{id}/telefones")
	public ResponseEntity<?> obterTelefones(@PathVariable Long id) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!podeAcessarUsuario(usuarioOp.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<>(usuarioOp.get().getTelefones(), HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/{id}/telefones")
	public ResponseEntity<?> adicionarTelefone(@PathVariable Long id, @RequestBody Telefone telefone) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!podeModificarUsuario(usuarioOp.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Usuario usuario = usuarioOp.get();
		usuario.getTelefones().add(telefone);
		Usuario salvo = repositorioUsuario.save(usuario);
		usuarioHdr.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/{id}/emails")
	public ResponseEntity<?> obterEmails(@PathVariable Long id) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!podeAcessarUsuario(usuarioOp.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<>(usuarioOp.get().getEmails(), HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/{id}/emails")
	public ResponseEntity<?> adicionarEmail(@PathVariable Long id, @RequestBody Email email) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!podeModificarUsuario(usuarioOp.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Usuario usuario = usuarioOp.get();
		usuario.getEmails().add(email);
		Usuario salvo = repositorioUsuario.save(usuario);
		usuarioHdr.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/{id}/documentos")
	public ResponseEntity<?> obterDocumentos(@PathVariable Long id) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!podeAcessarUsuario(usuarioOp.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<>(usuarioOp.get().getDocumentos(), HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/{id}/documentos")
	public ResponseEntity<?> adicionarDocumento(@PathVariable Long id, @RequestBody Documento documento) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!podeModificarUsuario(usuarioOp.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Usuario usuario = usuarioOp.get();
		usuario.getDocumentos().add(documento);
		Usuario salvo = repositorioUsuario.save(usuario);
		usuarioHdr.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/{id}/endereco")
	public ResponseEntity<?> obterEndereco(@PathVariable Long id) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty() || usuarioOp.get().getEndereco() == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!podeAcessarUsuario(usuarioOp.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<>(usuarioOp.get().getEndereco(), HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@PutMapping("/{id}/endereco")
	public ResponseEntity<?> atualizarEndereco(@PathVariable Long id, @RequestBody Endereco endereco) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!podeModificarUsuario(usuarioOp.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Usuario usuario = usuarioOp.get();
		usuario.setEndereco(endereco);
		Usuario salvo = repositorioUsuario.save(usuario);
		usuarioHdr.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	// Associacoes com Credencial (Restrita ao ADMINISTRADOR)
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@GetMapping("/{id}/credenciais")
	public ResponseEntity<Set<Credencial>> obterCredenciais(@PathVariable Long id) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Set<Credencial> credenciais = usuarioOp.get().getCredenciais();
		credenciais.forEach(c -> credencialHdr.adicionarLink(c));
		return new ResponseEntity<>(credenciais, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@PostMapping("/{id}/credenciais/{idCredencial}")
	public ResponseEntity<Usuario> associarCredencial(@PathVariable Long id, @PathVariable Long idCredencial) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		Optional<Credencial> credencialOp = repositorioCredencial.findById(idCredencial);
		if (usuarioOp.isEmpty() || credencialOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = usuarioOp.get();
		usuario.getCredenciais().add(credencialOp.get());
		Usuario atualizado = repositorioUsuario.save(usuario);
		usuarioHdr.adicionarLink(atualizado);
		return new ResponseEntity<>(atualizado, HttpStatus.OK);
	}

	// Veiculos do usuario
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/{id}/veiculos")
	public ResponseEntity<?> obterVeiculos(@PathVariable Long id) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = usuarioOp.get();
		if (!podeAcessarUsuario(usuario)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Set<Veiculo> veiculos = usuario.getVeiculos();
		veiculos.forEach(v -> veiculoHdr.adicionarLink(v));
		return new ResponseEntity<>(veiculos, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@PostMapping("/{id}/veiculos/{idVeiculo}")
	public ResponseEntity<?> associarVeiculo(@PathVariable Long id, @PathVariable Long idVeiculo) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		Optional<Veiculo> veiculoOp = repositorioVeiculo.findById(idVeiculo);
		if (usuarioOp.isEmpty() || veiculoOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = usuarioOp.get();
		if (!podeModificarUsuario(usuario)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Veiculo veiculo = veiculoOp.get();
		veiculo.setProprietario(usuario);
		repositorioVeiculo.save(veiculo);
		usuario.getVeiculos().add(veiculo);
		Usuario atualizado = repositorioUsuario.save(usuario);
		usuarioHdr.adicionarLink(atualizado);
		return new ResponseEntity<>(atualizado, HttpStatus.OK);
	}

	// Vendas do usuario
	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR', 'CLIENTE')")
	@GetMapping("/{id}/vendas")
	public ResponseEntity<?> obterVendas(@PathVariable Long id) {
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(id);
		if (usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Usuario usuario = usuarioOp.get();
		if (!podeAcessarUsuario(usuario)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Set<Venda> vendas = usuario.getVendas();
		vendas.forEach(v -> vendaHdr.adicionarLink(v));
		return new ResponseEntity<>(vendas, HttpStatus.OK);
	}
}
