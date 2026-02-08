package com.cjconfecciones.back.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ESTAMPADO")
public class Estampado extends Producto{
}
