package com.cjconfecciones.back.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "TPERSONA", schema = "cjconfecciones")
public class Persona {
    @Id
    private String cedula;
    private String nombre;
    private String apellido;
    private String telefono;
    private String direccion;
}
