/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.rest.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.auth.AuthenticationException;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.commons.userregistration.UserFactory;
import org.jevis.rest.Config;
import org.jevis.rest.json.JsonUser;

/**
 *
 * @author Florian Simon
 */
@Path("/JEWebService/v1/registration")
public class RegisterService {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postUser(
            @Context HttpHeaders httpHeaders,
            JsonUser user
    ) {
        if (!hasAccess(httpHeaders)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        JEVisDataSource ds = null;

        try {

            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "POST User ");
            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "username: " + user.getLogin());
            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "userpassword: " + user.getPassword());
            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "email: " + user.getEmail());
            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "firstname: " + user.getFirstname());
            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "lastname: " + user.getLastname());
            Logger.getLogger(RegisterService.class.getName()).log(Level.INFO, "organisation: " + user.getOrganisation());

            if (!userExists(Config.getDBConnection(), user.getLogin())) {
                ds = Config.geSysAdminDS();
                JEVisObject obj = ds.getObject(Config.getDemoRoot());

                if (obj == null) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Demouser root is not configured").build();
                }

                List<JEVisObject> demoGroups = new ArrayList<>();
                if (Config.getDemoGroup() > 0) {
                    JEVisObject demoGroup = ds.getObject(Config.getDemoGroup());
                    if (demoGroup != null) {
                        demoGroups.add(demoGroup);
                    } else {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Demogroup does not exist").build();
                    }

                }

                boolean isCreated = UserFactory.buildMobileDemoStructure(ds, obj, user.getLogin(), user.getPassword(), user.getEmail(), user.getFirstname(), user.getLastname(), user.getOrganisation(), demoGroups);

                if (isCreated) {
//                    return Response.status(Response.Status.OK).build();
                    return Response.ok(user).build();
                } else {
                    return Response.status(Response.Status.CONFLICT).build();
                }
            } else {
                return Response.status(Response.Status.CONFLICT).build();
            }

        } catch (Exception jex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jex.toString()).build();
        } finally {
            Config.CloseDS(ds);
        }

    }

    /**
     * fast dirty solution to make the registraion service a bit more save. The
     * registraion service works without a valid JEVis user and can create new
     * User.
     *
     * @param httpHeaders
     * @return
     */
    private boolean hasAccess(HttpHeaders httpHeaders) {
        if (httpHeaders.getRequestHeader("registraionapikey") == null || httpHeaders.getRequestHeader("registraionapikey").isEmpty()) {
            Logger.getLogger(RegisterService.class.getName()).log(Level.SEVERE, "Missing registraionapikey header");
            return false;
        }
        String auth = httpHeaders.getRequestHeader("registraionapikey").get(0);
        if (auth != null && !auth.isEmpty()) {
            return auth.equals(Config.getRigestrationAPIKey());
        }
        return false;

    }

    private boolean userExists(Connection con, String username) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            ps = con.prepareStatement("select id from object where type=? and name=? limit 1");
            ps.setString(1, "User");
            ps.setString(2, username);

            rs = ps.executeQuery();

            while (rs.next()) {
                Logger.getLogger(RegisterService.class.getName()).log(Level.SEVERE, "User allready exists: " + username);
                return true;
            }
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            ps = null;
            rs = null;
        }
        return false;
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(
            @Context HttpHeaders httpHeaders,
            @PathParam("name") String name
    ) {
        Logger.getLogger(RegisterService.class.getName()).log(Level.SEVERE, "GET User: " + name);
        if (!hasAccess(httpHeaders)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
//            Connection con = Config.getDBConnection();
//            ps = con.prepareStatement("select id from object where type=? and name=? limit 1");
//            ps.setString(1, "User");
//            ps.setString(2, name);
//            System.out.println("Request: " + ps.toString());

            if (name == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity("name parameter is missing").build();
            }

            if (userExists(Config.getDBConnection(), name)) {
                //TODO: return existing user?
                JsonUser user = new JsonUser();
                user.setLogin(name);
                user.setEmail("Placeholder");
                user.setFirstname("Placeholder");
                user.setLastname("Placeholder");
                user.setOrganisation("Placeholder");
                user.setPassword("Placeholder");
                return Response.ok(user).build();
//                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getCause().getMessage()).build();
        } finally {

        }

    }

}
