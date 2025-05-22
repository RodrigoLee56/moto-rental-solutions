package com.motorental.motorental_backend.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.motorental.motorental_backend.dto.MotoDTO;
import com.motorental.motorental_backend.exception.MotoNaoEncontradaException;
import com.motorental.motorental_backend.exception.UsuarioNaoEncontradoException;
import com.motorental.motorental_backend.model.Moto;
import com.motorental.motorental_backend.model.Usuario;
import com.motorental.motorental_backend.repository.MotoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MotoService {

	@Value("${app.upload-dir}")
	private String uploadDir;

	@Autowired
	private final MotoRepository motoRepository;

	@Autowired
	private final UsuarioService usuarioService;

	public void initialize() {
        System.out.println("Upload directory: " + uploadDir);
    }
	
	public List<Moto> listar() {
		return motoRepository.findAll();
	}

	public Moto buscarPorId(Long id) {
		return motoRepository.findById(id).orElseThrow(() -> new MotoNaoEncontradaException("Moto não encontrada"));
	}

	public Moto cadastrar(MotoDTO dto) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();

		Usuario vendedor = usuarioService.findByEmail(email)
				.orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));

		Moto moto = new Moto();
		moto.setMarca(dto.getMarca());
		moto.setModelo(dto.getModelo());
		moto.setAno(dto.getAno());
		moto.setPrecoPorDia(dto.getPrecoPorDia());
		moto.setCategoria(dto.getCategoria());
		moto.setImagem(dto.getImagem());
		moto.setDisponivel(dto.getDisponivel());
		moto.setVendedor(vendedor);

		return motoRepository.save(moto);
	}

	public Moto atualizar(Long id, MotoDTO dto) {
		Moto moto = buscarPorId(id);
		moto.setMarca(dto.getMarca());
		moto.setModelo(dto.getModelo());
		moto.setAno(dto.getAno());
		moto.setPrecoPorDia(dto.getPrecoPorDia());
		moto.setCategoria(dto.getCategoria());
		moto.setImagem(dto.getImagem());
		moto.setDisponivel(dto.getDisponivel());
		return motoRepository.save(moto);
	}

	public void excluir(Long id) {
		motoRepository.deleteById(id);
	}

	public ResponseEntity<String> uploadImagem(Long id, MultipartFile file) {
        // 1. Buscar moto pelo ID
        Moto moto = motoRepository.findById(id)
                .orElseThrow(() -> new MotoNaoEncontradaException("Moto não encontrada"));

        // 2. Validar arquivo
        if (file.isEmpty()) {
            throw new RuntimeException("Arquivo de imagem é obrigatório");
        }

        // 3. Validar tipo do arquivo (JPEG ou PNG)
        if (!Arrays.asList("image/jpeg", "image/png").contains(file.getContentType())) {
            throw new RuntimeException("Tipo de arquivo inválido. Aceitamos apenas JPEG e PNG.");
        }

        // 4. Validar tamanho do arquivo (máximo 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("Arquivo muito grande. Tamanho máximo permitido: 5MB");
        }

        // 5. Garantir que diretório uploads exista
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs(); // Cria a pasta automaticamente
        }

        // 6. Gerar nome único com UUID + extensão correta
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID() + fileExtension;
        Path filePath = Paths.get(uploadDir).resolve(uniqueFileName);

        // 7. Salvar arquivo no sistema
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar o arquivo", e);
        }

        // 8. Atualizar campo imagem na entidade Moto
        moto.setImagem(uniqueFileName);
        motoRepository.save(moto);

        return ResponseEntity.ok("Imagem carregada com sucesso: " + uniqueFileName);
    }
}
