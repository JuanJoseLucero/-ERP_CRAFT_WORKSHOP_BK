package com.cjconfecciones.back.util;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;

import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@RequestScoped
public class Util {

    private Logger log = Logger.getLogger(Util.class.getName());

    public Long getNextVal(EntityManager em, String nombreSecuencia){
        Long respuesta = null;
        try{
            String queryGeneric = "select nextval('"+nombreSecuencia+"')";
            respuesta = Long.valueOf(String.valueOf(em.createNativeQuery(queryGeneric).getSingleResult()));
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR GET TO NEXT VALUE ".concat(nombreSecuencia) ,e );
        }
        return respuesta;
    }

    public String nextValueProduct(EntityManager em, String nombreSecuencia, String numDigitos){
        String respuesta = null;
        try{
            Long secuencia = getNextVal(em,nombreSecuencia);
            return String.format("%0" + numDigitos + "d", secuencia);
        }catch (Exception e){
            log.log(Level.SEVERE, "ERROR GET TO NEXT VALUE PRODUCTS ".concat(nombreSecuencia) ,e );
        }
        return respuesta;
    }
}
