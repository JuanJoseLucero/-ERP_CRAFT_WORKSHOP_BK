package com.cjconfecciones.back.pojos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Cliente {
    private Integer id;
    private String nombre;
    private String apellido;
    private String telefono;
    private String direccion;
}
