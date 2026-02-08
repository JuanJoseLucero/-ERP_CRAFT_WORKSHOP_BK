package com.cjconfecciones.back.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CONFECCION")
public class Confeccion extends Producto {
}
