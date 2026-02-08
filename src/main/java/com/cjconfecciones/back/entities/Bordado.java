package com.cjconfecciones.back.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@Entity
@DiscriminatorValue("B")
public class Bordado extends Producto {
    private BigDecimal puntadas;
    private BigDecimal valorpuntada;
    private BigDecimal valordiseniocalculado;
    private BigDecimal valordiseniofinal;
}
