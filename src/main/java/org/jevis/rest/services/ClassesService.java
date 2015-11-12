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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.security.sasl.AuthenticationException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.rest.ClassIcon;
import org.jevis.rest.Config;
import org.jevis.rest.IconCache;
import org.jevis.rest.JsonFactory;
import org.jevis.rest.json.JsonJEVisClass;

/**
 * This class handels all the JEVIsOBjects related requests
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Path("/JEWebService/v1/classes")
public class ClassesService {

    /**
     * Returns an List of JEVisClasses as Json
     *
     * @param context
     * @return
     * @throws JEVisException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
            @Context HttpHeaders httpHeaders) throws JEVisException {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(ClassesService.class.getName()).log(Level.INFO, "GET Classes");
            //TODO: we could cache the classes because there are very static most of the time
            ds = Config.getJEVisDS(httpHeaders);

            List<JEVisClass> classes = ds.getJEVisClasses();
            List<JsonJEVisClass> jclasses = JsonFactory.buildJEVisClass(classes);
            JsonJEVisClass[] retrunArray = jclasses.toArray(new JsonJEVisClass[jclasses.size()]);
            return Response.ok(retrunArray).build();

        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * Returns the requested JEVisClass
     *
     * @param context
     * @param name
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{name}")
    public Response getJEVisClass(
            @Context HttpHeaders httpHeaders,
            @PathParam("name") String name) {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(ClassesService.class.getName()).log(Level.INFO, "GET Class: " + name);

            ds = Config.getJEVisDS(httpHeaders);
            JEVisClass jclass = ds.getJEVisClass(name);
            return Response.ok(JsonFactory.buildJEVisClass(jclass)).build();

        } catch (JEVisException jex) {
            jex.printStackTrace();
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * Returns the Icon of the requested JEVisClass
     *
     * @param context
     * @param name
     * @return
     */
    @GET
    @Path("/{name}/icon")
    public Response getClassIcon(
            @Context HttpHeaders httpHeaders,
            @PathParam("name") String name) {

        JEVisDataSource ds = null;
        try {
            Logger.getLogger(ClassesService.class.getName()).log(Level.INFO, "GET ClassesIcon: " + name);

            if (IconCache.getInstance().getIcon(name).getIconBytes() != null) {
                return Response.ok(new ByteArrayInputStream(IconCache.getInstance().getIcon(name).getIconBytes()), MediaType.valueOf("image/png")).build();
            } else {
                ds = Config.getJEVisDS(httpHeaders);

                JEVisClass jclass = ds.getJEVisClass(name);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                try {
                    ImageIO.write(jclass.getIcon(), "png", baos);
                } catch (IOException ex) {
                    Logger.getLogger(ClassesService.class.getName()).log(Level.SEVERE, null, ex);
                }
                byte[] imageData = baos.toByteArray();
                IconCache.getInstance().addIcon(name, new ClassIcon(name, imageData));
                return Response.ok(new ByteArrayInputStream(imageData), MediaType.valueOf("image/png")).build();

            }

        } catch (JEVisException jex) {
            Logger.getLogger(ClassesService.class.getName()).log(Level.SEVERE, null, jex);
            return Response.serverError().build();
        } catch (ExecutionException ex) {
            Logger.getLogger(ClassesService.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        } catch (AuthenticationException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(ex.getMessage()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

}
