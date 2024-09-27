package com.cjconfecciones.back.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name= "TPRODUCTO", schema = "cjconfecciones")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String codigosri;
    private String descripcion;
    private BigDecimal valorunitario;
    private String tipoproducto;
    private String tipoproductosri;
}

