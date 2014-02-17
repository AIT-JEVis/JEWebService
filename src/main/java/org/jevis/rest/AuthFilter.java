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

/**
 * see web.xml for activation
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Provider
public class AuthFilter implements ContainerRequestFilter {

    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {

        String auth = containerRequest.getHeaderValue("authorization");
        if (auth != null && !auth.isEmpty()) {
//
            auth = auth.replaceFirst("[Bb]asic ", "");
            String userColonPass = Base64.base64Decode(auth);
            System.out.println("userColonPass: " + userColonPass);
            String username = "";
            String password = "";

            if (Base64.isBase64(auth)) {
                String[] dauth = (new String(Base64.decode(auth))).split(":");
                if (dauth.length == 2) {
                    username = dauth[0];
                    password = dauth[1];

                } else {
//                    throw new JEVisException("Auth header must contain login and password separated by a colon!", JEVisExceptionCodes.EMPTY_PARAMETER);
                }
            } else {
//                throw new JEVisException("Auth header must be valid Base64!", JEVisExceptionCodes.EMPTY_PARAMETER);
            }


            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
        } else {
            //todo trow auth error
        }

        return containerRequest;
    }
}
