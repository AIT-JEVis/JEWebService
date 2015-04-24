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

/**
 * This Class holds the userinformation and will be used as key ba the cache
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class User {

    public String username;
    public String role;
    /**
     * Will be used as uniqeu indetifyer
     */
    public String base64;

    /**
     * Create an user for the given parameter
     *
     * @param username
     * @param role
     * @param auth
     */
    public User(String username, String role, String auth) {
        this.username = username;
        this.role = role;
        this.base64 = auth;
    }
}
