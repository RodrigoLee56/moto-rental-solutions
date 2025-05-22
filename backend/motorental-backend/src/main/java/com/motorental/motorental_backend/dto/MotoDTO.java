package com.motorental.motorental_backend.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MotoDTO {
	private Long id;
    private String marca;
    private String modelo;
    private Integer ano;
    private BigDecimal precoPorDia;
    private String categoria;
    private String imagem;
    private Boolean disponivel;
}
