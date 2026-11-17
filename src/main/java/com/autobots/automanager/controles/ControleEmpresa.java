package com.autobots.automanager.controles;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.hateoas.EmpresaHdr;
import com.autobots.automanager.hateoas.MercadoriaHdr;
import com.autobots.automanager.hateoas.ServicoHdr;
import com.autobots.automanager.hateoas.UsuarioHdr;
import com.autobots.automanager.hateoas.VendaHdr;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioServico;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVenda;

@RestController
@RequestMapping("/empresas")
public class ControleEmpresa {

	@Autowired
	private RepositorioEmpresa repositorioEmpresa;

	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Autowired
	private RepositorioMercadoria repositorioMercadoria;

	@Autowired
	private RepositorioServico repositorioServico;

	@Autowired
	private RepositorioVenda repositorioVenda;

	@Autowired
	private EmpresaHdr empresaHdr;

	@Autowired
	private UsuarioHdr usuarioHdr;

	@Autowired
	private MercadoriaHdr mercadoriaHdr;

	@Autowired
	private ServicoHdr servicoHdr;

	@Autowired
	private VendaHdr vendaHdr;

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@GetMapping
	public ResponseEntity<List<Empresa>> obterEmpresas() {
		List<Empresa> empresas = repositorioEmpresa.findAll();
		if (empresas.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		empresaHdr.adicionarLink(empresas);
		return new ResponseEntity<>(empresas, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@GetMapping("/{id}")
	public ResponseEntity<Empresa> obterEmpresa(@PathVariable Long id) {
		Optional<Empresa> empresaOp = repositorioEmpresa.findById(id);
		if (empresaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Empresa empresa = empresaOp.get();
		empresaHdr.adicionarLink(empresa);
		return new ResponseEntity<>(empresa, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@PostMapping
	public ResponseEntity<Empresa> cadastrarEmpresa(@RequestBody Empresa empresa) {
		if (empresa.getCadastro() == null) {
			empresa.setCadastro(new Date());
		}
		Empresa salva = repositorioEmpresa.save(empresa);
		empresaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.CREATED);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@PutMapping("/{id}")
	public ResponseEntity<Empresa> atualizarEmpresa(@PathVariable Long id, @RequestBody Empresa atualizacao) {
		Optional<Empresa> empresaOp = repositorioEmpresa.findById(id);
		if (empresaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Empresa empresa = empresaOp.get();
		if (atualizacao.getRazaoSocial() != null) {
			empresa.setRazaoSocial(atualizacao.getRazaoSocial());
		}
		if (atualizacao.getNomeFantasia() != null) {
			empresa.setNomeFantasia(atualizacao.getNomeFantasia());
		}
		if (atualizacao.getEndereco() != null) {
			empresa.setEndereco(atualizacao.getEndereco());
		}
		if (atualizacao.getTelefones() != null && !atualizacao.getTelefones().isEmpty()) {
			empresa.getTelefones().clear();
			empresa.getTelefones().addAll(atualizacao.getTelefones());
		}
		Empresa salva = repositorioEmpresa.save(empresa);
		empresaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluirEmpresa(@PathVariable Long id) {
		Optional<Empresa> empresaOp = repositorioEmpresa.findById(id);
		if (empresaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		repositorioEmpresa.delete(empresaOp.get());
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@GetMapping("/{id}/usuarios")
	public ResponseEntity<Set<Usuario>> obterUsuarios(@PathVariable Long id) {
		Optional<Empresa> empresaOp = repositorioEmpresa.findById(id);
		if (empresaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Set<Usuario> usuarios = empresaOp.get().getUsuarios();
		usuarios.forEach(u -> usuarioHdr.adicionarLink(u));
		return new ResponseEntity<>(usuarios, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@PostMapping("/{id}/usuarios/{idUsuario}")
	public ResponseEntity<Empresa> associarUsuario(@PathVariable Long id, @PathVariable Long idUsuario) {
		Optional<Empresa> empresaOp = repositorioEmpresa.findById(id);
		Optional<Usuario> usuarioOp = repositorioUsuario.findById(idUsuario);
		if (empresaOp.isEmpty() || usuarioOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Empresa empresa = empresaOp.get();
		empresa.getUsuarios().add(usuarioOp.get());
		Empresa salva = repositorioEmpresa.save(empresa);
		empresaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/{id}/mercadorias")
	public ResponseEntity<Set<Mercadoria>> obterMercadorias(@PathVariable Long id) {
		Optional<Empresa> empresaOp = repositorioEmpresa.findById(id);
		if (empresaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Set<Mercadoria> mercadorias = empresaOp.get().getMercadorias();
		mercadorias.forEach(m -> mercadoriaHdr.adicionarLink(m));
		return new ResponseEntity<>(mercadorias, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@PostMapping("/{id}/mercadorias/{idMercadoria}")
	public ResponseEntity<Empresa> associarMercadoria(@PathVariable Long id, @PathVariable Long idMercadoria) {
		Optional<Empresa> empresaOp = repositorioEmpresa.findById(id);
		Optional<Mercadoria> mercadoriaOp = repositorioMercadoria.findById(idMercadoria);
		if (empresaOp.isEmpty() || mercadoriaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Empresa empresa = empresaOp.get();
		empresa.getMercadorias().add(mercadoriaOp.get());
		Empresa salva = repositorioEmpresa.save(empresa);
		empresaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE', 'VENDEDOR')")
	@GetMapping("/{id}/servicos")
	public ResponseEntity<Set<Servico>> obterServicos(@PathVariable Long id) {
		Optional<Empresa> empresaOp = repositorioEmpresa.findById(id);
		if (empresaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Set<Servico> servicos = empresaOp.get().getServicos();
		servicos.forEach(s -> servicoHdr.adicionarLink(s));
		return new ResponseEntity<>(servicos, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@PostMapping("/{id}/servicos/{idServico}")
	public ResponseEntity<Empresa> associarServico(@PathVariable Long id, @PathVariable Long idServico) {
		Optional<Empresa> empresaOp = repositorioEmpresa.findById(id);
		Optional<Servico> servicoOp = repositorioServico.findById(idServico);
		if (empresaOp.isEmpty() || servicoOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Empresa empresa = empresaOp.get();
		empresa.getServicos().add(servicoOp.get());
		Empresa salva = repositorioEmpresa.save(empresa);
		empresaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE')")
	@GetMapping("/{id}/vendas")
	public ResponseEntity<Set<Venda>> obterVendas(@PathVariable Long id) {
		Optional<Empresa> empresaOp = repositorioEmpresa.findById(id);
		if (empresaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Set<Venda> vendas = empresaOp.get().getVendas();
		vendas.forEach(v -> vendaHdr.adicionarLink(v));
		return new ResponseEntity<>(vendas, HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMINISTRADOR')")
	@PostMapping("/{id}/vendas/{idVenda}")
	public ResponseEntity<Empresa> associarVenda(@PathVariable Long id, @PathVariable Long idVenda) {
		Optional<Empresa> empresaOp = repositorioEmpresa.findById(id);
		Optional<Venda> vendaOp = repositorioVenda.findById(idVenda);
		if (empresaOp.isEmpty() || vendaOp.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Empresa empresa = empresaOp.get();
		empresa.getVendas().add(vendaOp.get());
		Empresa salva = repositorioEmpresa.save(empresa);
		empresaHdr.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}
}
