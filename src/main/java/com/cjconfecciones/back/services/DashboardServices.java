package com.cjconfecciones.back.services;

import com.cjconfecciones.back.controllers.AbonoController;
import com.cjconfecciones.back.controllers.DashboardController;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.logging.Logger;

@Path("dashboard")
public class DashboardServices {

    Logger log = Logger.getLogger(DashboardServices.class.getName());

    @Inject
    private DashboardController dashboardController;

    @POST
    @Path("/getDataDashboard")
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject getDataDashboard(){
        return dashboardController.getDataDashboard();
    }
}
