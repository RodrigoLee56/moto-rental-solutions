package com.motorental.motorental_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.motorental.motorental_backend.dto.UsuarioDTO;
import com.motorental.motorental_backend.exception.UsuarioNaoEncontradoException;
import com.motorental.motorental_backend.model.Usuario;
import com.motorental.motorental_backend.repository.UsuarioRepository;
import com.motorental.motorental_backend.security.JwtUtil;

@Service
public class UsuarioService {
	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public Usuario save(UsuarioDTO dto) {
		if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
			throw new RuntimeException("Email já cadastrado");
		}

		Usuario usuario = new Usuario();
		usuario.setNome(dto.getNome());
		usuario.setEmail(dto.getEmail());
		usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
		usuario.setTelefone(dto.getTelefone());
		usuario.setEndereco(dto.getEndereco());
		usuario.setRole(dto.getRole());

		return usuarioRepository.save(usuario);
	}

	public Optional<Usuario> findByEmail(String email) {
		return usuarioRepository.findByEmail(email);
	}

	public Usuario update(Long id, UsuarioDTO dto) {
		Usuario usuario = usuarioRepository.findById(id)
				.orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));

		// Atualiza apenas os campos fornecidos no DTO
		usuario.setNome(dto.getNome());
		usuario.setEmail(dto.getEmail());
		usuario.setTelefone(dto.getTelefone());
		usuario.setEndereco(dto.getEndereco());
		usuario.setRole(dto.getRole());

		// Se a senha for fornecida, criptografa-a
		if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
			usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
		}

		return usuarioRepository.save(usuario);
	}

	public Usuario findById(Long id) {
		return usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
	}

	public List<Usuario> findAll() {
		return usuarioRepository.findAll();
	}

	public boolean deleteById(Long id) {
		Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);

		if (usuarioOptional.isPresent()) {
			usuarioRepository.deleteById(id);
			return true;
		}

		return false;
	}

}
