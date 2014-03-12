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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jevis.jeapi.JEVisClass;
import org.jevis.jeapi.JEVisException;
import org.jevis.rest.Config;
import org.jevis.rest.JsonFactory;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/api/rest/class")
public class ClassService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{name}")
    public Response getClass(@PathParam("name") String name) {
        try {
            Config config = new Config();
            JEVisClass jclass = config.getDS("Sys Admin", "jevis").getJEVisClass(name);
            System.out.println("Class: " + jclass);

            return Response.ok(JsonFactory.buildJEVisClass(jclass)).build();
        } catch (JEVisException ex) {
            return Response.status(404).entity(ex.getMessage()).build();
        }

    }
}