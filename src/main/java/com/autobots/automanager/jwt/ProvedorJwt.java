package com.autobots.automanager.jwt;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;

import io.jsonwebtoken.Claims;
import lombok.Data;

@Data
@Component
public class ProvedorJwt {
	@Value("${jwt.secret:defaultSecretKeyForJwtAuthenticationAutoManagerSecurity2026!}")
	private String assinatura;
	
	@Value("${jwt.expiration:3600000}")
	private Long duracao;

	public String proverJwt(Usuario usuario, String nomeUsuario) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("id", usuario.getId());
		claims.put("usuario", nomeUsuario);
		claims.put("perfil", usuario.getPerfis().stream().map(PerfilUsuario::name).collect(Collectors.toList()));
		
		GeradorJwt gerador = new GeradorJwt(assinatura, duracao);
		return gerador.gerarJwt(nomeUsuario, claims);
	}

	public boolean validarJwt(String jwt) {
		AnalisadorJwt analisador = new AnalisadorJwt(assinatura, jwt);
		ValidadorJwt validador = new ValidadorJwt();
		return validador.validar(analisador.obterReivindicacoes());
	}

	public String obterNomeUsuario(String jwt) {
		AnalisadorJwt analisador = new AnalisadorJwt(assinatura, jwt);
		Claims reivindicacoes = analisador.obterReivindicacoes();
		return analisador.obterNomeUsuairo(reivindicacoes);
	}

	public Claims obterClaims(String jwt) {
		AnalisadorJwt analisador = new AnalisadorJwt(assinatura, jwt);
		return analisador.obterReivindicacoes();
	}
}
