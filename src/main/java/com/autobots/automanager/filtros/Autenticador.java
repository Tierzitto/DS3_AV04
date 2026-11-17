package com.autobots.automanager.filtros;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.autobots.automanager.adaptadores.UserDetailsImpl;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;
import com.autobots.automanager.jwt.ProvedorJwt;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Autenticador extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager gerenciadorAutenticacao;
	private ProvedorJwt provedorJwt;

	public Autenticador(AuthenticationManager gerenciadorAutenticacao, ProvedorJwt provedorJwt) {
		this.gerenciadorAutenticacao = gerenciadorAutenticacao;
		this.provedorJwt = provedorJwt;
		setFilterProcessesUrl("/login");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		CredencialUsuarioSenha credencial = null;
		try {
			credencial = new ObjectMapper().readValue(request.getInputStream(), CredencialUsuarioSenha.class);
		} catch (IOException e) {
			credencial = new CredencialUsuarioSenha();
			credencial.setNomeUsuario("");
			credencial.setSenha("");
		}
		UsernamePasswordAuthenticationToken dadosAutenticacao = new UsernamePasswordAuthenticationToken(
				credencial.getNomeUsuario(), credencial.getSenha(), Collections.emptyList());
		return gerenciadorAutenticacao.authenticate(dadosAutenticacao);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication autenticacao) throws IOException, ServletException {
		UserDetailsImpl userDetails = (UserDetailsImpl) autenticacao.getPrincipal();
		String nomeUsuario = userDetails.getUsername();
		String jwt = provedorJwt.proverJwt(userDetails.getUsuario(), nomeUsuario);

		response.addHeader("Authorization", "Bearer " + jwt);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		Map<String, Object> corpoResposta = new HashMap<>();
		corpoResposta.put("token", jwt);
		corpoResposta.put("tipo", "Bearer");
		corpoResposta.put("usuario", nomeUsuario);
		corpoResposta.put("id", userDetails.getUsuario().getId());
		corpoResposta.put("perfis", userDetails.getUsuario().getPerfis());

		new ObjectMapper().writeValue(response.getOutputStream(), corpoResposta);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		Map<String, Object> erro = new HashMap<>();
		erro.put("status", HttpServletResponse.SC_UNAUTHORIZED);
		erro.put("mensagem", "Falha de autenticação: usuário ou senha inválidos.");

		new ObjectMapper().writeValue(response.getOutputStream(), erro);
	}
}
