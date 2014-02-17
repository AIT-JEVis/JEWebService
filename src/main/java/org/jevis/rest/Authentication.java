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
import org.jevis.jeapi.JEVisException;
import org.jevis.jeapi.JEVisExceptionCodes;

/**
 *
 * @author Florian Simon<florian.simon@openjevis.org>
 */
public class Authentication {

//    public static final String HEADER_PARAMETER = "Authorization";
    private String _username;
    private String _password;

    public Authentication(String base64auth) throws JEVisException {
        if (base64auth != null) {
            String authkey = base64auth.split(" ")[1];
            if (Base64.isBase64(authkey)) {
                String[] dauth = (new String(Base64.decode(authkey))).split(":");
                if (dauth.length == 2) {
                    _username = dauth[0];
                    _password = dauth[1];
                } else {
                    throw new JEVisException("Auth header must contain login and password separated by a colon!", JEVisExceptionCodes.EMPTY_PARAMETER);
                }
            } else {
                throw new JEVisException("Auth header must be valid Base64!", JEVisExceptionCodes.EMPTY_PARAMETER);
            }
        } else {
            throw new JEVisException("Auth header must be specified!", JEVisExceptionCodes.EMPTY_PARAMETER);
        }
    }

    public Authentication() {
        _username = "Sys Admin";
        _password = "jevis";
    }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }
}
