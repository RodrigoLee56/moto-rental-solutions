package com.motorental.motorental_backend.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.motorental.motorental_backend.dto.MotoDTO;
import com.motorental.motorental_backend.model.Moto;
import com.motorental.motorental_backend.service.MotoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/motos")
public class MotoController {

	@Autowired
	private MotoService motoService;
	

	@GetMapping
	public ResponseEntity<List<Moto>> listar() {
		return ResponseEntity.ok(motoService.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Moto> buscar(@PathVariable Long id) {
		return ResponseEntity.ok(motoService.buscarPorId(id));
	}

	@PostMapping
	public ResponseEntity<Moto> cadastrar(@RequestBody @Valid MotoDTO dto) {
		Moto salva = motoService.cadastrar(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(salva);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Moto> atualizar(@PathVariable Long id, @RequestBody @Valid MotoDTO dto) {
		return ResponseEntity.ok(motoService.atualizar(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		motoService.excluir(id);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/{id}/upload")
    @PreAuthorize("hasRole('VENDEDOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<String> uploadImagem(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return motoService.uploadImagem(id, file);
    }

}
