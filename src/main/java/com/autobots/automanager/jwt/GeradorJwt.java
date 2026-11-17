package com.autobots.automanager.jwt;

import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

class GeradorJwt {
	private String assinatura;
	private Date expiracao;

	public GeradorJwt(String assinatura, long duracao) {
		this.assinatura = assinatura;
		this.expiracao = new Date(System.currentTimeMillis() + duracao);
	}

	public String gerarJwt(String nomeUsuario, Map<String, Object> claims) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(nomeUsuario)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(this.expiracao)
				.signWith(SignatureAlgorithm.HS512, this.assinatura.getBytes())
				.compact();
	}
}
