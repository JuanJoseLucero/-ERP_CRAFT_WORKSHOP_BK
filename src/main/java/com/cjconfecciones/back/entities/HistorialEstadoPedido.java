package com.cjconfecciones.back.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "thistorialestadopedido", schema = "cjconfecciones")
public class HistorialEstadoPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer cpedido;
    private Integer cestado;
    private Date fecha;
    private String usuario;
    private String observacion;
    private Short notificacionMal;
    private Short notificacionMovil;
}
