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

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.ext.Provider;
import org.joda.time.DateTime;

/**
 *
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Provider
public class DebugFilter implements ContainerRequestFilter {

    private static final boolean enable = true;

    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {
        if (!enable) {
            return containerRequest;
        }

        String base64Auth = "";
        if (containerRequest.getRequestHeader("authorization") != null && !containerRequest.getRequestHeader("authorization").isEmpty()) {
            base64Auth = containerRequest.getRequestHeader("authorization").get(0);
        }
        String mesg = String.format("Request Time:[%s]  Path:[%s]  Method:[%s]  MediaType:[%s]  Auth:[%s]", DateTime.now().toString(), containerRequest.getPath(), containerRequest.getMethod(), containerRequest.getMediaType(), base64Auth);

        Logger.getLogger(DebugFilter.class.getName()).log(Level.INFO, mesg);

        return containerRequest;
    }

}
