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

import org.jevis.jeapi.JEVisDataSource;
import org.jevis.jeapi.JEVisException;
import org.jevis.jeapi.sql.JEVisDataSourceSQL;

/**
 *
 * @author Florian Simon<florian.simon@envidatec.com>
 */
public class Config {

    public static String _dbport = "3306";
    public static String _dbip = "localhost";
    public static String _ip = "localhost";
    public static String _port = "5007";
    public static String _dbuser = "jevis";
    public static String _dbpw = "jevistest";
    public static String _schema = "jevis";

    public static String getDBHost() {
        return "192.168.2.55";
    }

    public static String getDBPort() {
        return "3306";
    }

    public static String getDBUser() {
        return "jevis";
    }

    public static String getDBPW() {
        return "jevistest";
    }

    public static String getSchema() {
        return "jevis";
    }

    public static JEVisDataSource getDS(String username, String pw) throws JEVisException {
        System.out.println(String.format("Connect to %s %s %s %s %s", getDBHost(), getDBPort(), getSchema(), getDBUser(), getDBPW()));
        JEVisDataSource ds = new JEVisDataSourceSQL(
                getDBHost(), getDBPort(), getSchema(),
                getDBUser(), getDBPW(),
                username, pw);
        if (ds.connect(username, pw)) {
            System.out.println("Connected user: " + username);
        } else {
            System.out.println(" connection faild");
        }

        return ds;

    }
}
