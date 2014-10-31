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

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;
import org.jevis.rest.JEVisConnectionCache;
import org.jevis.rest.JsonFactory;
import org.jevis.rest.json.JsonType;

/**
 *
 * THis Class handels all the JEVisType requests
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/JEWebService/v1/classes/{name}/types")
public class TypeService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @Context SecurityContext context,
            @Context HttpHeaders httpHeaders,
            @PathParam("name") String name) throws JEVisException {
        System.out.println("getTypes");
        JEVisDataSource ds = JEVisConnectionCache.getInstance().getDataSource(context.getUserPrincipal().getName());

        JEVisClass jclass = ds.getJEVisClass(name);

        List<JEVisType> typs = jclass.getTypes();
        List<JsonType> jtypes = JsonFactory.buildTypes(typs);
        JsonType[] retrunArray = jtypes.toArray(new JsonType[jtypes.size()]);

        System.out.println("return total: " + retrunArray.length);
        return Response.ok(retrunArray).build();
    }

}
