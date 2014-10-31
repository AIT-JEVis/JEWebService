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

import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;

/**
 * This Filter handels the Authentification with the API.
 *
 * see web.xml for activation
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Provider
public class AuthFilter implements ContainerRequestFilter {

    public final static String HTTP_HEADER_USER = "JEVis-ID";

    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {

        System.out.println("Path: " + containerRequest.getPath());
//        if(WhiteList.starts(path)){
//            //allow without auth 
//        }

        //Example "Basic U3lzIEFkbWluOmpldmlz"
        String auth = containerRequest.getHeaderValue("authorization");

        if (auth != null && !auth.isEmpty()) {

            auth = auth.replaceFirst("[Bb]asic ", "");
//            String userColonPass = Base64.base64Decode(auth);

            if (Base64.isBase64(auth)) {
                String[] dauth = (new String(Base64.decode(auth))).split(":");
                if (dauth.length == 2) {
                    String username = dauth[0];
                    String password = dauth[1];

                    User user = new User(username, password, auth);
                    if (JEVisConnectionCache.getInstance().contains(auth)) {
//                        System.out.println("User is allready cached");
                        containerRequest.setSecurityContext(new Authorizer(user));
                    } else {
                        try {
                            //check if the login is ok
                            //TODO: handel deffreent errors
                            JEVisDataSource ds = Config.getDS(username, password);
                            ds.getCurrentUser();//TODO. check more...

                            containerRequest.setSecurityContext(new Authorizer(user));
                            JEVisConnectionCache.getInstance().addUser(auth, ds);
                        } catch (JEVisException ex) {
                            throw ErrorBuilder.ErrorBuilder(Response.Status.UNAUTHORIZED.getStatusCode(), 2001, "Username/Password is not correct.");
                        }
                    }

                } else {
                    throw ErrorBuilder.ErrorBuilder(Response.Status.BAD_REQUEST.getStatusCode(), 2002, "The HTML authorization header is not correct formate");
                }
            } else {
                throw ErrorBuilder.ErrorBuilder(Response.Status.BAD_REQUEST.getStatusCode(), 2003, "The HTML authorization header is not in Base64");
            }
        } else {
            throw ErrorBuilder.ErrorBuilder(Response.Status.BAD_REQUEST.getStatusCode(), 2004, "The HTML authorization header is missing or emty");
        }

        return containerRequest;
    }

}
