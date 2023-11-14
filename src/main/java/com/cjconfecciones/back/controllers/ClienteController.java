package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.pojos.Cliente;
import com.cjconfecciones.back.pojos.Persona;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.*;
import jakarta.transaction.Transaction;

import java.io.Serializable;
import java.util.logging.Logger;

@Named
@RequestScoped
public class ClienteController implements Serializable {

    @PersistenceUnit(unitName = "unitPersistence")
    private EntityManagerFactory emf;
    //private EntityManagerFactory emf = Persistence.createEntityManagerFactory("unitPersistence");

    Logger log = Logger.getLogger(ClienteController.class.getName());

    public void newClient (){
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        log.info("NUEVA PERSONA");
        Persona p = new Persona();
        p.setCedula("0104809470");
        em.persist(p);
        transaction.commit();
        log.info("INSERTO CLIENTE");
    }
}
