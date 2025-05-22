package com.motorental.motorental_backend.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Moto {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	private String marca;
    private String modelo;
    private Integer ano;
    private BigDecimal precoPorDia;
    private String categoria;
    private String imagem;
    private Boolean disponivel;
    
    @ManyToOne
    private Usuario vendedor;
}
