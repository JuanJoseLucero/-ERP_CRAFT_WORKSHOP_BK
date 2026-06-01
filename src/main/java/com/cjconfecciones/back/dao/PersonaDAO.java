package com.cjconfecciones.back.dao;

import com.cjconfecciones.back.entities.Persona;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

public class PersonaDAO {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("unitPersistence");

    public List<Persona> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Persona p ORDER BY p.nombre", Persona.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Persona findById(String cedula) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Persona.class, cedula);
        } finally {
            em.close();
        }
    }

    public void persist(Persona persona) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(persona);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void update(Persona persona) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(persona);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void delete(String cedula) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Persona persona = em.find(Persona.class, cedula);
            if (persona != null) {
                em.remove(persona);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
