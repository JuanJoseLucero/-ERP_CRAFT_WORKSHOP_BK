package com.cjconfecciones.back.util;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ApplicationScoped
@NoArgsConstructor
public class Propiedades implements Serializable {

	private Properties properties;
	static Logger log = Logger.getLogger(Propiedades.class.getName());

	@PostConstruct
	public void init() {
		properties = new Properties();
	}

	public String getParametrosProperties(String data) {
		try {
			properties.load(getClass().getClassLoader().getResourceAsStream("parametros.properties"));
		} catch (Exception e) {
			log.log(Level.SEVERE, "PROBLEMAS AL OBTENER CONSULTAS DE PROOVEDORES", e);
		}
		return properties.getProperty(data);
	}
	
}
