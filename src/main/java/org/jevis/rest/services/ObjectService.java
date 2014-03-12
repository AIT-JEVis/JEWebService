/**
 * Copyright (C) 2013 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEWebService.
 *
 * JEWebService is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEWebService is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEWebService. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEWebService is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.rest.services;

import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jevis.jeapi.JEVisClass;
import org.jevis.jeapi.JEVisDataSource;
import org.jevis.jeapi.JEVisException;
import org.jevis.jeapi.JEVisObject;
import org.jevis.jeapi.JEVisRelationship;
import org.jevis.rest.Config;
import org.jevis.rest.JsonFactory;
import org.jevis.rest.json.JsonObject;

/**
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/api/rest/objects")
public class ObjectService {

    /**
     *
     * @param id
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Response getObject(@PathParam("id") long id) {
        try {
            Config config = new Config();
            JEVisDataSource ds = config.getDS("Sys Admin", "jevis");

            JEVisObject obj = ds.getObject(id);
            System.out.println("Obj: " + obj);
            return Response.ok(JsonFactory.buildObject(obj)).build();
        } catch (JEVisException ex) {
            return Response.status(404).entity(ex.getMessage()).build();
        }

    }

    /**
     *
     * @param id
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<JsonObject> get(
            @QueryParam("class") String jclass) throws JEVisException {
        List<JsonObject> jsons = new LinkedList<JsonObject>();

        if (jclass == null) {
            JEVisDataSource ds = Config.getDS("Sys Admin", "jevis");

            List<JEVisObject> roots = ds.getRootObjects();
            for (JEVisObject o : roots) {
                jsons.add(JsonFactory.buildObject(o));
            }
        } else {



            Config config = new Config();
            JEVisDataSource ds = Config.getDS("Sys Admin", "jevis");

            JEVisClass jevClass = ds.getJEVisClass(jclass);
            List<JEVisObject> objects = ds.getObjects(jevClass, true);

            for (JEVisObject o : objects) {
                jsons.add(JsonFactory.buildObject(o));
            }
        }

        return jsons;

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("application/json")
    public Response post(JsonObject json) {
        try {
            System.out.println("create new obj: " + json.getName() + " " + json.getJevisClass() + " " + json.getParent());
            JEVisDataSource ds = Config.getDS("Sys Admin", "jevis");

            JEVisObject obj = ds.getObject(json.getParent());
            JEVisClass jevClass = ds.getJEVisClass(json.getJevisClass());


            JEVisObject newObj = obj.buildObject(json.getName(), jevClass);

//            newObj.commit();

            return Response.ok(JsonFactory.buildObject(newObj)).build();
        } catch (JEVisException ex) {
            return Response.status(404).entity(ex.getMessage()).build();
        }

    }
}
