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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.sql.JEVisDataSourceSQL;

/**
 *
 * @author Florian Simon<florian.simon@envidatec.com>
 */
public class Config {

    //@Singleton
    public static String _dbport = "3306";
    public static String _dbip = "192.168.2.55";
    public static String _ip = "localhost";
    public static String _port = "5007";
    public static String _dbuser = "jevis";
    public static String _dbpw = "jevistest";
    public static String _schema = "jevis";
    private static boolean _loadFromFile = true;

    public static String getDBHost() {
        return _dbip;
    }

    public static String getDBPort() {
        return _dbport;
    }

    public static String getDBUser() {
        return _dbuser;
    }

    public static String getDBPW() {
        return _dbpw;
    }

    public static String getSchema() {
        return _schema;
    }

    public static JEVisDataSource getDS(String username, String pw) throws JEVisException {

        //TODO: i think we can replace this funktion by using the Grizzly server
        if (_loadFromFile) {
            try {
                XMLConfiguration config = new XMLConfiguration("config.xml");
                _port = config.getString("webservice.port");
                _dbport = config.getString("datasource.port");
                _dbip = config.getString("datasource.url");
                _dbuser = config.getString("datasource.login");
                _dbpw = config.getString("datasource.password");
                _schema = config.getString("datasource.schema");
            } catch (ConfigurationException ex) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
            }
            _loadFromFile = false;
        }

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
