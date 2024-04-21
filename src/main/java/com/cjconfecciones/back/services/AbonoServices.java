package com.cjconfecciones.back.services;

import com.cjconfecciones.back.controllers.AbonoController;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.logging.Logger;

@Path("abono")
public class AbonoServices {

    Logger log = Logger.getLogger(PersonServices.class.getName());

    @Inject
    private AbonoController abonoController;

    @POST
    @Path("/getLst4cabecera")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject getOrderById(JsonObject jsonObject){
        return abonoController.getLstAbonos(jsonObject);
    }

    @POST
    @Path("/persistAbono")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject persistAbono(JsonObject jsonObject){
        return abonoController.persistAbonos(jsonObject);
    }

}
