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

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.hateoas.CredencialHdr;
import com.autobots.automanager.repositorios.RepositorioCredencial;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/credenciais")
public class ControleCredencial {

	@Autowired
	private RepositorioCredencial repositorioCredencial;

	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Autowired
	private CredencialHdr credencialHdr;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@GetMapping
	public ResponseEntity<List<Credencial>> obterCredenciais() {
		List<Credencial> credenciais = repositorioCredencial.findAll();
		if (credenciais.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		credencialHdr.adicionarLink(credenciais);
		return new ResponseEntity<>(credenciais, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@GetMapping("/{id}")
	public ResponseEntity<Credencial> obterCredencial(@PathVariable Long id) {
		Optional<Credencial> credencialOp = repositorioCredencial.findById(id);
		if (credencialOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Credencial credencial = credencialOp.get();
		credencialHdr.adicionarLink(credencial);
		return new ResponseEntity<>(credencial, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@PostMapping
	public ResponseEntity<Credencial> cadastrarCredencial(@RequestBody Credencial credencial) {
		if (credencial.getCriacao() == null) {
			credencial.setCriacao(new Date());
		}
		if (credencial instanceof CredencialUsuarioSenha) {
			CredencialUsuarioSenha cus = (CredencialUsuarioSenha) credencial;
			if (cus.getSenha() != null && !cus.getSenha().startsWith("$2a$")) {
				cus.setSenha(passwordEncoder.encode(cus.getSenha()));
			}
		}
		Credencial salva = repositorioCredencial.save(credencial);
		credencialHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.CREATED);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@PutMapping("/{id}")
	public ResponseEntity<Credencial> atualizarCredencial(@PathVariable Long id, @RequestBody Credencial atualizacao) {
		Optional<Credencial> credencialOp = repositorioCredencial.findById(id);
		if (credencialOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Credencial credencial = credencialOp.get();
		credencial.setInativo(atualizacao.isInativo());
		if (atualizacao.getUltimoAcesso() != null) {
			credencial.setUltimoAcesso(atualizacao.getUltimoAcesso());
		}
		if (credencial instanceof CredencialUsuarioSenha && atualizacao instanceof CredencialUsuarioSenha) {
			CredencialUsuarioSenha cus = (CredencialUsuarioSenha) credencial;
			CredencialUsuarioSenha nova = (CredencialUsuarioSenha) atualizacao;
			if (nova.getNomeUsuario() != null) {
				cus.setNomeUsuario(nova.getNomeUsuario());
			}
			if (nova.getSenha() != null && !nova.getSenha().isBlank()) {
				cus.setSenha(passwordEncoder.encode(nova.getSenha()));
			}
		} else if (credencial instanceof CredencialCodigoBarra && atualizacao instanceof CredencialCodigoBarra) {
			CredencialCodigoBarra ccb = (CredencialCodigoBarra) credencial;
			CredencialCodigoBarra nova = (CredencialCodigoBarra) atualizacao;
			if (nova.getCodigo() > 0) {
				ccb.setCodigo(nova.getCodigo());
			}
		}
		Credencial salva = repositorioCredencial.save(credencial);
		credencialHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluirCredencial(@PathVariable Long id) {
		Optional<Credencial> credencialOp = repositorioCredencial.findById(id);
		if (credencialOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		repositorioCredencial.delete(credencialOp.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@PostMapping("/{id}/usuario/{idUsuario}")
	public ResponseEntity<Credencial> associarAoUsuario(@PathVariable Long id, @PathVariable Long idUsuario) {
		Optional<Credencial> credencialOp = repositorioCredencial.findById(id);
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(idUsuario);
		if (credencialOp.isEmpty() || usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Credencial credencial = credencialOp.get();
		Usuario usuario = usuarioOp.get();
		usuario.getCredenciais().add(credencial);
		repositorioUsuario.save(usuario);
		credencialHdr.adicionarLink(credencial);
		return new ResponseEntity<>(credencial, HttpStatus.OK);
	}
}
