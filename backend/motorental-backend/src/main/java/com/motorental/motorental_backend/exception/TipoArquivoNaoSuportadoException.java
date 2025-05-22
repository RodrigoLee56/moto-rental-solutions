package com.motorental.motorental_backend.exception;

public class TipoArquivoNaoSuportadoException extends RuntimeException {
	public TipoArquivoNaoSuportadoException(String message) {
		super(message);
	}
}