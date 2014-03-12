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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jevis.jeapi.JEVisClass;
import org.jevis.jeapi.JEVisDataSource;
import org.jevis.jeapi.JEVisException;
import org.jevis.jeapi.JEVisType;
import org.jevis.rest.Config;
import org.jevis.rest.JsonFactory;
import org.jevis.rest.json.JsonType;

/**
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
@Path("/api/rest/classes/{class}/types")
public class TypeService {

    /**
     *
     * @param id
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<JsonType> get(@PathParam("class") String classname) throws JEVisException {
        JEVisDataSource ds = Config.getDS("Sys Admin", "jevis");
        JEVisClass jclass = ds.getJEVisClass(classname);

        List<JEVisType> types = jclass.getTypes();
        List<JsonType> jsons = new LinkedList<JsonType>();
        for (JEVisType type : types) {
            jsons.add(JsonFactory.buildType(type));
        }


        return jsons;

    }

    /**
     *
     * @param type
     * @param classname
     * @return
     * @throws JEVisException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}")
    public Response getByName(
            @QueryParam("type") String type,
            @PathParam("class") String classname) throws JEVisException {

        JEVisDataSource ds = Config.getDS("Sys Admin", "jevis");

        JEVisClass jevClass = ds.getJEVisClass(classname);

        return Response.ok(JsonFactory.buildType(jevClass.getType(type))).build();

    }
}
