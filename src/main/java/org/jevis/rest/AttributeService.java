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
package org.jevis.rest;

import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jevis.jeapi.JEVisAttribute;
import org.jevis.jeapi.JEVisException;
import org.jevis.jeapi.JEVisObject;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
//@Path("/objects/{id}/attributes/{attribute}")
@Path("/api/rest/objects/{id}/attributes/")
public class AttributeService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{attribute}")
    public Response getAttribute(
            @PathParam("id") long id,
            @PathParam("attribute") String attribute) {
        try {
            System.out.println("getAttributes");
            Config config = new Config();
            JEVisObject obj = config.getDS("Sys Admin", "jevis").getObject(id);
            JEVisAttribute att = obj.getAttribute(attribute);

            return Response.ok(JsonFactory.buildAttribute(att)).build();
        } catch (JEVisException ex) {
            return Response.status(404).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<JsonAttribute> getAll(
            @PathParam("id") long id,
            @PathParam("attribute") String attribute) throws JEVisException {
        System.out.println("getAttributes");
        Config config = new Config();
        JEVisObject obj = config.getDS("Sys Admin", "jevis").getObject(id);
        List<JsonAttribute> attributes = new LinkedList<JsonAttribute>();
        for (JEVisAttribute att : obj.getAttributes()) {
            attributes.add(JsonFactory.buildAttribute(att));
        }

        return attributes;
    }
}
