package com.motorental.motorental_backend.exception;

import java.nio.file.AccessDeniedException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler({ IllegalArgumentException.class, ArquivoInvalidoException.class,
			TamanhoArquivoExcedidoException.class, TipoArquivoNaoSuportadoException.class })
	public ResponseEntity<String> handleBadRequestExceptions(RuntimeException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}

	@ExceptionHandler({ MotoNaoEncontradaException.class, UsuarioNaoEncontradoException.class })
	public ResponseEntity<String> handleNotFoundExceptions(RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleGeneralException(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("Ocorreu um erro inesperado: " + ex.getMessage());
	}
}
