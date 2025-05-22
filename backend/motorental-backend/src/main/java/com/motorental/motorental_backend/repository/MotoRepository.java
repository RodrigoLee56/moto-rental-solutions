package com.motorental.motorental_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.motorental.motorental_backend.model.Moto;

public interface MotoRepository extends JpaRepository<Moto, Long> {
    List<Moto> findByDisponivel(Boolean disponivel);
}