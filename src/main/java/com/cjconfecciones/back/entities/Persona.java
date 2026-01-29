package com.cjconfecciones.back.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
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
@NamedQuery(
        name = "Persona.buscarPorNombre",
        query = "SELECT p FROM Persona p WHERE p.nombre LIKE :nombre"
)
public class Persona {
    @Id
    private String cedula;
    private String nombre;
    private String telefono;
    private String direccion;
}
