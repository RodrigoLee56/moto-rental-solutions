package com.motorental.motorental_backend.dto;

import com.motorental.motorental_backend.model.Role;

import lombok.Data;

@Data
public class UsuarioDTO {
	private String nome;
	private String email;
	private String senha;
	private String telefone;
	private String endereco;
	private Role role;
}
