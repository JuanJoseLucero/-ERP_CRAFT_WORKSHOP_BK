package com.cjconfecciones.back.controllers;

import com.cjconfecciones.back.entities.Persona;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Habilita Mockito en JUnit 5
@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    // Crea el controller real e INYECTA los mocks en sus campos (@PersistenceUnit, @PersistenceContext)
    @InjectMocks
    private ClienteController controller;

    @Mock
    private EntityManager em;

    @Mock
    private EntityManagerFactory emf;

    @Mock
    private EntityTransaction transaction;

    private Persona personaExistente;

    @BeforeEach
    void setUp() {
        personaExistente = new Persona("0104809470", "Juan Jose Lucero", "0998348972", "Avenida de las americas", "JUANJOLUCERO@HOTMAIL.ES", true);
    }

    // ─────────────────────────────────────────
    // getAll()
    // ─────────────────────────────────────────
    @Test
    void getAll_devuelveLista() {
        TypedQuery<Persona> query = mock(TypedQuery.class);
        when(em.createQuery("SELECT p FROM Persona p WHERE (p.activo IS NULL OR p.activo = true) ORDER BY p.nombre", Persona.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(
                personaExistente,
                new Persona("9999999999", "Otro Cliente", "0999999999", "Dir", "otro@test.com", true)
        ));

        JsonObject response = controller.getAll();

        assertEquals("0", response.getString("error"));
        assertEquals(2, response.getJsonArray("personas").size());
        assertEquals("Juan Jose Lucero", response.getJsonArray("personas").getJsonObject(0).getString("nombre"));
        assertEquals("Otro Cliente", response.getJsonArray("personas").getJsonObject(1).getString("nombre"));
    }

    @Test
    void getAll_errorDevuelveError() {
        when(em.createQuery(anyString(), any())).thenThrow(new RuntimeException("DB Error"));

        JsonObject response = controller.getAll();

        assertEquals("1", response.getString("error"));
    }

    // ─────────────────────────────────────────
    // getById()
    // ─────────────────────────────────────────
    @Test
    void getById_cuandoExiste_devuelvePersona() {
        when(em.find(Persona.class, "0104809470")).thenReturn(personaExistente);

        JsonObject request = Json.createObjectBuilder().add("cedula", "0104809470").build();
        JsonObject response = controller.getById(request);

        assertEquals("0", response.getString("error"));
        assertEquals("0104809470", response.getString("cedula"));
        assertEquals("Juan Jose Lucero", response.getString("nombre"));
        assertEquals("0998348972", response.getString("telefono"));
        assertEquals("Avenida de las americas", response.getString("direccion"));
        assertEquals("JUANJOLUCERO@HOTMAIL.ES", response.getString("email"));
    }

    @Test
    void getById_cuandoNoExiste_devuelveError() {
        when(em.find(Persona.class, "0000000000")).thenReturn(null);

        JsonObject request = Json.createObjectBuilder().add("cedula", "0000000000").build();
        JsonObject response = controller.getById(request);

        assertEquals("1", response.getString("error"));
        assertEquals("Cliente no encontrado", response.getString("mensaje"));
    }

    // ─────────────────────────────────────────
    // create()
    // ─────────────────────────────────────────
    @Test
    void create_cuandoNoExiste_guardaYretornaOK() {
        // Simula que la cédula NO existe
        when(em.find(Persona.class, "9999999999")).thenReturn(null);
        // emf.createEntityManager() devuelve el mismo EntityManager mock
        when(emf.createEntityManager()).thenReturn(em);
        when(em.getTransaction()).thenReturn(transaction);

        JsonObject request = Json.createObjectBuilder()
                .add("cedula", "9999999999")
                .add("nombre", "Nuevo Cliente")
                .add("telefono", "0999999999")
                .add("direccion", "Direccion Test")
                .add("email", "nuevo@test.com")
                .build();

        JsonObject response = controller.create(request);

        assertEquals("0", response.getString("error"));
        assertEquals("9999999999", response.getString("cedula"));

        // Verifica que persist() se haya llamado con una Persona
        ArgumentCaptor<Persona> captor = ArgumentCaptor.forClass(Persona.class);
        verify(em).persist(captor.capture());
        assertEquals("Nuevo Cliente", captor.getValue().getNombre());
        assertEquals("0999999999", captor.getValue().getTelefono());

        verify(transaction).begin();
        verify(transaction).commit();
    }

    @Test
    void create_cuandoYaExiste_devuelveError() {
        when(em.find(Persona.class, "0104809470")).thenReturn(personaExistente);

        JsonObject request = Json.createObjectBuilder()
                .add("cedula", "0104809470")
                .add("nombre", "Juan")
                .build();

        JsonObject response = controller.create(request);

        assertEquals("1", response.getString("error"));
        assertEquals("La cédula ya existe", response.getString("mensaje"));
        // persist NUNCA debe ejecutarse
        verify(em, never()).persist(any());
    }

    // ─────────────────────────────────────────
    // update()
    // ─────────────────────────────────────────
    @Test
    void update_cuandoExiste_actualizaYretornaOK() throws Exception {
        // Creamos persona original
        Persona original = new Persona("0104809470", "Nombre Original", "111111", "Direccion Original", "original@test.com", true);
        when(em.find(Persona.class, "0104809470")).thenReturn(original);
        when(emf.createEntityManager()).thenReturn(em);
        when(em.getTransaction()).thenReturn(transaction);

        JsonObject request = Json.createObjectBuilder()
                .add("cedula", "0104809470")
                .add("nombre", "Nombre Actualizado")
                .add("telefono", "222222")
                .build();

        JsonObject response = controller.update(request);

        assertEquals("0", response.getString("error"));

        // El objeto original debe haberse modificado
        assertEquals("Nombre Actualizado", original.getNombre());
        assertEquals("222222", original.getTelefono());
        // Los campos que NO se enviaron deben conservar su valor
        assertEquals("Direccion Original", original.getDireccion());
        assertEquals("original@test.com", original.getEmail());

        verify(em).merge(original);
        verify(transaction).begin();
        verify(transaction).commit();
    }

    @Test
    void update_cuandoNoExiste_devuelveError() {
        when(em.find(Persona.class, "0000000000")).thenReturn(null);

        JsonObject request = Json.createObjectBuilder().add("cedula", "0000000000").build();
        JsonObject response = controller.update(request);

        assertEquals("1", response.getString("error"));
        assertEquals("Cliente no encontrado", response.getString("mensaje"));
    }

    // ─────────────────────────────────────────
    // delete()
    // ─────────────────────────────────────────
    @Test
    void delete_cuandoExiste_eliminaYretornaOK() {
        when(em.find(Persona.class, "0104809470")).thenReturn(personaExistente);
        when(emf.createEntityManager()).thenReturn(em);
        when(em.getTransaction()).thenReturn(transaction);
        // el segundo find (emTrans.find) tambien devuelve la persona
        when(em.find(Persona.class, "0104809470")).thenReturn(personaExistente);

        JsonObject request = Json.createObjectBuilder().add("cedula", "0104809470").build();
        JsonObject response = controller.delete(request);

        assertEquals("0", response.getString("error"));

        assertFalse(personaExistente.getActivo());
        ArgumentCaptor<Persona> captor = ArgumentCaptor.forClass(Persona.class);
        verify(em).merge(captor.capture());
        assertFalse(captor.getValue().getActivo());
        assertEquals("0104809470", captor.getValue().getCedula());
        verify(transaction).begin();
        verify(transaction).commit();
    }

    @Test
    void delete_cuandoNoExiste_devuelveError() {
        when(em.find(Persona.class, "0000000000")).thenReturn(null);

        JsonObject request = Json.createObjectBuilder().add("cedula", "0000000000").build();
        JsonObject response = controller.delete(request);

        assertEquals("1", response.getString("error"));
        assertEquals("Cliente no encontrado", response.getString("mensaje"));
    }
}
