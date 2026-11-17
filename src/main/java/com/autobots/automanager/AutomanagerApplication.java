package com.autobots.automanager;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.enumeracoes.TipoDocumento;
import com.autobots.automanager.enumeracoes.TipoVeiculo;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@SpringBootApplication
public class AutomanagerApplication implements CommandLineRunner {

	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Autowired
	private RepositorioEmpresa repositorioEmpresa;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${admin.username:admin}")
	private String adminUsername;

	@Value("${admin.password:admin123}")
	private String adminPassword;

	@Value("${admin.name:Administrador do Sistema}")
	private String adminName;

	public static void main(String[] args) {
		SpringApplication.run(AutomanagerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// 1. Criar administrador inicial condicionalmente (somente se não existir nenhum usuário com perfil ADMINISTRADOR)
		List<Usuario> todosUsuarios = repositorioUsuario.findAll();
		boolean existeAdmin = todosUsuarios.stream()
				.anyMatch(u -> u.getPerfis().contains(PerfilUsuario.ADMINISTRADOR));

		if (!existeAdmin) {
			Usuario admin = new Usuario();
			admin.setNome(adminName);
			admin.setNomeSocial("Admin");
			admin.getPerfis().add(PerfilUsuario.ADMINISTRADOR);

			CredencialUsuarioSenha credencialAdmin = new CredencialUsuarioSenha();
			credencialAdmin.setInativo(false);
			credencialAdmin.setNomeUsuario(adminUsername);
			credencialAdmin.setSenha(passwordEncoder.encode(adminPassword));
			credencialAdmin.setCriacao(new Date());
			credencialAdmin.setUltimoAcesso(new Date());

			admin.getCredenciais().add(credencialAdmin);
			repositorioUsuario.save(admin);
		}

		// 2. Se a base de empresas estiver vazia, cria a estrutura demonstrativa com os perfis GERENTE, VENDEDOR e CLIENTE
		if (repositorioEmpresa.count() == 0) {
			Empresa empresa = new Empresa();
			empresa.setRazaoSocial("Car service toyota ltda");
			empresa.setNomeFantasia("Car service manutenção veicular");
			empresa.setCadastro(new Date());

			Endereco enderecoEmpresa = new Endereco();
			enderecoEmpresa.setEstado("São Paulo");
			enderecoEmpresa.setCidade("São Paulo");
			enderecoEmpresa.setBairro("Centro");
			enderecoEmpresa.setRua("Av. São João");
			enderecoEmpresa.setNumero("00");
			enderecoEmpresa.setCodigoPostal("01035-000");
			empresa.setEndereco(enderecoEmpresa);

			Telefone telefoneEmpresa = new Telefone();
			telefoneEmpresa.setDdd("011");
			telefoneEmpresa.setNumero("986454527");
			empresa.getTelefones().add(telefoneEmpresa);

			// Gerente
			Usuario gerente = new Usuario();
			gerente.setNome("Carlos Silva Gerente");
			gerente.setNomeSocial("Carlos Gerente");
			gerente.getPerfis().add(PerfilUsuario.GERENTE);

			CredencialUsuarioSenha credencialGerente = new CredencialUsuarioSenha();
			credencialGerente.setInativo(false);
			credencialGerente.setNomeUsuario("gerente");
			credencialGerente.setSenha(passwordEncoder.encode("gerente123"));
			credencialGerente.setCriacao(new Date());
			credencialGerente.setUltimoAcesso(new Date());
			gerente.getCredenciais().add(credencialGerente);
			empresa.getUsuarios().add(gerente);

			// Vendedor
			Usuario vendedor = new Usuario();
			vendedor.setNome("Marcos Oliveira Vendedor");
			vendedor.setNomeSocial("Marcos Vendedor");
			vendedor.getPerfis().add(PerfilUsuario.VENDEDOR);

			CredencialUsuarioSenha credencialVendedor = new CredencialUsuarioSenha();
			credencialVendedor.setInativo(false);
			credencialVendedor.setNomeUsuario("vendedor");
			credencialVendedor.setSenha(passwordEncoder.encode("vendedor123"));
			credencialVendedor.setCriacao(new Date());
			credencialVendedor.setUltimoAcesso(new Date());
			vendedor.getCredenciais().add(credencialVendedor);
			empresa.getUsuarios().add(vendedor);

			// Cliente
			Usuario cliente = new Usuario();
			cliente.setNome("Pedro Alcantara Cliente");
			cliente.setNomeSocial("Dom Pedro Cliente");
			cliente.getPerfis().add(PerfilUsuario.CLIENTE);

			CredencialUsuarioSenha credencialCliente = new CredencialUsuarioSenha();
			credencialCliente.setInativo(false);
			credencialCliente.setNomeUsuario("cliente");
			credencialCliente.setSenha(passwordEncoder.encode("cliente123"));
			credencialCliente.setCriacao(new Date());
			credencialCliente.setUltimoAcesso(new Date());
			cliente.getCredenciais().add(credencialCliente);

			Veiculo veiculo = new Veiculo();
			veiculo.setPlaca("ABC-0000");
			veiculo.setModelo("corolla-cross");
			veiculo.setTipo(TipoVeiculo.SUV);
			veiculo.setProprietario(cliente);
			cliente.getVeiculos().add(veiculo);

			empresa.getUsuarios().add(cliente);

			// Mercadorias
			Mercadoria rodaLigaLeve = new Mercadoria();
			rodaLigaLeve.setCadastro(new Date());
			rodaLigaLeve.setFabricao(new Date());
			rodaLigaLeve.setNome("Roda de liga leve modelo toyota");
			rodaLigaLeve.setValidade(new Date());
			rodaLigaLeve.setQuantidade(30);
			rodaLigaLeve.setValor(300.0);
			rodaLigaLeve.setDescricao("Roda de liga leve original de fábrica");
			empresa.getMercadorias().add(rodaLigaLeve);

			// Serviços
			Servico trocaRodas = new Servico();
			trocaRodas.setDescricao("Troca das rodas do carro por novas");
			trocaRodas.setNome("Troca de rodas");
			trocaRodas.setValor(50);
			empresa.getServicos().add(trocaRodas);

			// Venda
			Venda venda = new Venda();
			venda.setCadastro(new Date());
			venda.setIdentificacao("VENDA-001");
			venda.setCliente(cliente);
			venda.setFuncionario(vendedor);
			venda.setVeiculo(veiculo);
			venda.getMercadorias().add(rodaLigaLeve);
			venda.getServicos().add(trocaRodas);
			veiculo.getVendas().add(venda);
			empresa.getVendas().add(venda);

			repositorioEmpresa.save(empresa);
		}
	}
}
