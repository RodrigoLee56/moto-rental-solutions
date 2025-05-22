package com.motorental.motorental_backend.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.motorental.motorental_backend.dto.MotoDTO;
import com.motorental.motorental_backend.exception.ArquivoInvalidoException;
import com.motorental.motorental_backend.exception.MotoNaoEncontradaException;
import com.motorental.motorental_backend.exception.TamanhoArquivoExcedidoException;
import com.motorental.motorental_backend.exception.TipoArquivoNaoSuportadoException;
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

	private final MotoRepository motoRepository;

	private final UsuarioService usuarioService;

	public void initialize() {
        System.out.println("Upload directory: " + uploadDir);
    }
	
	public List<Moto> listar() {
		return motoRepository.findAll();
	}

	public Moto buscarPorId(Long id) {
        return motoRepository.findById(id)
                .orElseThrow(() -> new MotoNaoEncontradaException("Moto não encontrada com ID: " + id));
    }

	public Moto cadastrar(MotoDTO dto) {
        validarDadosMoto(dto);
        
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
        validarDadosMoto(dto);
        Moto moto = buscarPorId(id);
        try {
			verificarPermissoesUsuario(moto);
		} catch (AccessDeniedException e) {
			e.printStackTrace();
		}

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
        Moto moto = buscarPorId(id);
        try {
			verificarPermissoesUsuario(moto);
		} catch (AccessDeniedException e) {
			e.printStackTrace();
		}
        
        // Remover imagem associada se existir
        if (moto.getImagem() != null && !moto.getImagem().isEmpty()) {
            Path imagemPath = Paths.get(uploadDir).resolve(moto.getImagem());
            try {
                Files.deleteIfExists(imagemPath);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao remover imagem da moto", e);
            }
        }
        
        motoRepository.deleteById(id);
    }

	public String uploadImagem(Long id, MultipartFile file) {
        try {
            Moto moto = buscarPorId(id);
            verificarPermissoesUsuario(moto);
            validarArquivo(file);
            criarDiretorioUpload();
            
            String nomeArquivo = salvarArquivo(file);
            atualizarImagemMoto(moto, nomeArquivo);
            
            return nomeArquivo;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar upload: " + e.getMessage(), e);
        }
    }
	
	private void validarDadosMoto(MotoDTO dto) {
		if (dto.getPrecoPorDia().compareTo(BigDecimal.ZERO) <= 0) {
	        throw new IllegalArgumentException("Preço por dia deve ser positivo");
	    }
	    if (dto.getAno() < 1900 || dto.getAno() > java.time.Year.now().getValue()) {
	        throw new IllegalArgumentException("Ano da moto inválido");
	    }
    }

    private void verificarPermissoesUsuario(Moto moto) throws AccessDeniedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Usuario usuario = usuarioService.findByEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
        
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        
        if (!usuario.equals(moto.getVendedor()) && !isAdmin) {
            throw new AccessDeniedException("Você não tem permissão para modificar esta moto");
        }
    }

    private void validarArquivo(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ArquivoInvalidoException("Arquivo de imagem é obrigatório");
        }
        
        String contentType = file.getContentType();
        if (!Arrays.asList("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            throw new TipoArquivoNaoSuportadoException(
                    "Tipo de arquivo inválido. Aceitamos apenas JPEG, PNG e WEBP.");
        }
        
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new TamanhoArquivoExcedidoException(
                    "Arquivo muito grande. Tamanho máximo permitido: 5MB");
        }
    }

    private void criarDiretorioUpload() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    private String salvarArquivo(MultipartFile file) throws IOException {
        String extensao = extrairExtensao(file.getOriginalFilename());
        String nomeArquivo = UUID.randomUUID() + extensao;
        Path destino = Paths.get(uploadDir).resolve(nomeArquivo);
        
        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        return nomeArquivo;
    }

    private String extrairExtensao(String nomeArquivo) {
        return nomeArquivo != null && nomeArquivo.contains(".") 
                ? nomeArquivo.substring(nomeArquivo.lastIndexOf("."))
                : "";
    }

    private void atualizarImagemMoto(Moto moto, String nomeImagem) {
        // Se já existir uma imagem, deletar a antiga
        if (moto.getImagem() != null && !moto.getImagem().isEmpty()) {
            Path imagemAntiga = Paths.get(uploadDir).resolve(moto.getImagem());
            try {
                Files.deleteIfExists(imagemAntiga);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao remover imagem antiga", e);
            }
        }
        
        moto.setImagem(nomeImagem);
        motoRepository.save(moto);
    }
}
