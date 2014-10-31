/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
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

import java.security.Principal;
import javax.ws.rs.core.SecurityContext;

/**
 * This simple class implements the SecurityContext. We we it to get the current
 * user which has allready authentify by the AuthFilter to the resource
 * services.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class Authorizer implements SecurityContext {

    private User user;
    private Principal principal;

    /**
     * default cronstrucktor
     *
     * @param user
     */
    public Authorizer(final User user) {
        this.user = user;
        this.principal = new Principal() {

            @Override
            public String getName() {
                return user.base64;
            }
        };
    }

    @Override
    public Principal getUserPrincipal() {
        return this.principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return (role.equals(user.role));
    }

    @Override
    public boolean isSecure() {
        return true;
//        return "https".equals(uriInfo.getRequestUri().getScheme());
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }

}
