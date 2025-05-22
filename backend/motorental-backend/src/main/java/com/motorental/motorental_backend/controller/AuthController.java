package com.motorental.motorental_backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.motorental.motorental_backend.dto.UsuarioDTO;
import com.motorental.motorental_backend.model.Usuario;
import com.motorental.motorental_backend.security.CustomUserDetailsService;
import com.motorental.motorental_backend.security.JwtUtil;
import com.motorental.motorental_backend.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody UsuarioDTO dto) {
		try {
			Usuario usuario = usuarioService.save(dto);
			return ResponseEntity.ok(Map.of("message", "Usuário registrado com sucesso", "usuario", usuario));
		} catch (Exception e) {
			return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		String senha = request.get("senha");

		try {
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);

			if (!passwordEncoder.matches(senha, userDetails.getPassword())) {
				throw new RuntimeException("Senha incorreta");
			}

			String token = jwtUtil.generateToken(userDetails);

			return ResponseEntity.ok(Map.of("token", token));
		} catch (Exception e) {
			return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
		}
	}
}