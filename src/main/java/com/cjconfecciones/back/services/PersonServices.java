package com.cjconfecciones.back.services;

import com.cjconfecciones.back.controllers.ClienteController;
import jakarta.annotation.Generated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import java.util.logging.Logger;

@Path("teset")
public class PersonServices {

    @Inject
    private ClienteController clienteController;

    Logger log = Logger.getLogger(PersonServices.class.getName());

    @GET
    @Path("persist")
    public void persistPersonServices(){
        log.info("WEB SERVICES STARTED");
        clienteController.newClient();
    }
}
