package com.cjconfecciones.back.entities;

import jakarta.inject.Named;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "TPEDIDODETALLE", schema = "cjconfecciones")
public class PedidoDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Date fecha;
    private BigDecimal unidades;
    private String descripcion;
    private BigDecimal vunitario;
    private BigDecimal total;
    private Integer ccabecera;

}
