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
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jevis.api.JEVisException;

/**
 * THis service was an simple test for an create object form but is not working
 * or used
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/api/rest/form/object")
public class ObjectForm {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response get() throws JEVisException {

        String page = "<html><body<h1>Add JEVisObject</h1><form action=\"../objects\" method=\"post\"><p>Name : <input type=\"text\"name=\"name\" /></p><p>JEVisClass : <input type=\"text\" name=\"class\" /></p><p>Parent ID : <input type=\"text\" name=\"parentid\" /></p><input type=\"submit\" value=\"put\" /></form></body></html>";

        return Response.ok(page).build();

    }
}
