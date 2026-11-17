package com.autobots.automanager.configuracao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.autobots.automanager.adaptadores.UserDetailsServiceImpl;
import com.autobots.automanager.filtros.Autenticador;
import com.autobots.automanager.filtros.Autorizador;
import com.autobots.automanager.jwt.ProvedorJwt;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class Seguranca {

	private final UserDetailsServiceImpl servico;
	private final ProvedorJwt provedorJwt;

	public Seguranca(UserDetailsServiceImpl servico, ProvedorJwt provedorJwt) {
		this.servico = servico;
		this.provedorJwt = provedorJwt;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager)
			throws Exception {
		http.cors().and().csrf().disable();

		http.authorizeHttpRequests(auth -> auth
				.antMatchers("/login", "/h2-console/**").permitAll()
				.anyRequest().authenticated()
		);

		http.headers(headers -> headers.frameOptions().disable());

		http.addFilter(new Autenticador(authenticationManager, provedorJwt));
		http.addFilter(new Autorizador(authenticationManager, provedorJwt, servico));

		http.exceptionHandling(handling -> handling
				.authenticationEntryPoint(new CustomAuthenticationEntryPoint())
				.accessDeniedHandler(new CustomAccessDeniedHandler())
		);

		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
		fonte.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
		return fonte;
	}

	private static class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
		@Override
		public void commence(HttpServletRequest request, HttpServletResponse response,
				AuthenticationException authException) throws IOException, ServletException {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding("UTF-8");
			Map<String, Object> erro = new HashMap<>();
			erro.put("status", HttpServletResponse.SC_UNAUTHORIZED);
			erro.put("erro", "Unauthorized");
			erro.put("mensagem", "Acesso não autorizado: token ausente ou inválido.");
			erro.put("caminho", request.getRequestURI());
			new ObjectMapper().writeValue(response.getOutputStream(), erro);
		}
	}

	private static class CustomAccessDeniedHandler implements AccessDeniedHandler {
		@Override
		public void handle(HttpServletRequest request, HttpServletResponse response,
				AccessDeniedException accessDeniedException) throws IOException, ServletException {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding("UTF-8");
			Map<String, Object> erro = new HashMap<>();
			erro.put("status", HttpServletResponse.SC_FORBIDDEN);
			erro.put("erro", "Forbidden");
			erro.put("mensagem", "Acesso negado: permissão insuficiente para acessar este recurso.");
			erro.put("caminho", request.getRequestURI());
			new ObjectMapper().writeValue(response.getOutputStream(), erro);
		}
	}
}
