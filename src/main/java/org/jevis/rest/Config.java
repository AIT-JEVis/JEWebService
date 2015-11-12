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
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.sasl.AuthenticationException;
import javax.sql.DataSource;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.sql.ConnectionFactory;
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
    public static String _rootUser = "jevis";
    public static String _rootPW = "jevis";

    public static long _demoRoot = -1;
    public static long _demoGroup = -1;
    public static String _registratioKey = "";

    private static boolean _loadFromFile = true;
    private static boolean _fileIsLoaded = false;

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

        readConfigurationFile();

//        System.out.println(String.format("Connect to %s %s %s %s %s", getDBHost(), getDBPort(), getSchema(), getDBUser(), getDBPW()));
        JEVisDataSource ds = new JEVisDataSourceSQL(
                getDBHost(), getDBPort(), getSchema(),
                getDBUser(), getDBPW());

        return ds;

    }

    /**
     * Internal JEVisDataSource as root using an configured user
     *
     * @param username
     * @param pw
     * @return
     * @throws JEVisException
     */
    public static JEVisDataSource geSysAdminDS() throws JEVisException {

        readConfigurationFile();

        JEVisDataSource ds = new JEVisDataSourceSQL(
                getDBHost(), getDBPort(), getSchema(),
                getDBUser(), getDBPW());
        if (ds.connect(_rootUser, _rootPW)) {
            System.out.println("Connected sys admin: " + _rootUser);
        } else {
            System.out.println(" connection faild");
        }

        return ds;

    }

    private static void readConfigurationFile() {
        try {
            if (!_fileIsLoaded) {
                File cfile = new File("config.xml");
                if (cfile.exists()) {
                    Logger.getLogger(Config.class.getName()).log(Level.INFO, "using Configfile: " + cfile.getAbsolutePath());
                    XMLConfiguration config = new XMLConfiguration(cfile);
                    _port = config.getString("webservice.port");
                    _dbport = config.getString("datasource.port");
                    _dbip = config.getString("datasource.url");
                    _dbuser = config.getString("datasource.login");
                    _dbpw = config.getString("datasource.password");
                    _schema = config.getString("datasource.schema");

                    //Woraround solution for the registration service
                    _rootUser = config.getString("sysadmin.username");
                    _rootPW = config.getString("sysadmin.password");
                    _demoRoot = config.getLong("registration.root");
                    _demoGroup = config.getLong("registration.demogroup");
                    _registratioKey = config.getString("registration.apikey");

                    _fileIsLoaded = true;
                } else {
                    Logger.getLogger(Config.class.getName()).log(Level.SEVERE, "Warning configfile does not exist: " + cfile.getAbsolutePath());
                }

            }

        } catch (ConfigurationException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getRigestrationAPIKey() {
        readConfigurationFile();
        return _registratioKey;
    }

    public static long getDemoGroup() {
        readConfigurationFile();
        return _demoGroup;
    }

    public static long getDemoRoot() {
        readConfigurationFile();
        return _demoRoot;
    }

    public static Connection getDBConnection() throws SQLException {
        readConfigurationFile();
        ConnectionFactory.getInstance().registerMySQLDriver(_dbip, _dbport, _schema, _dbuser, _dbpw);
        return ConnectionFactory.getInstance().getConnection();
    }

    public static JEVisDataSource getJEVisDS(HttpHeaders httpHeaders) throws AuthenticationException {
        if (httpHeaders.getRequestHeader("authorization") == null || httpHeaders.getRequestHeader("authorization").isEmpty()) {
            throw new AuthenticationException("Authorization header is missing");
        }
        String auth = httpHeaders.getRequestHeader("authorization").get(0);
        if (auth != null && !auth.isEmpty()) {
            auth = auth.replaceFirst("[Bb]asic ", "");

            if (Base64.isBase64(auth)) {
                String[] dauth = (new String(Base64.decode(auth))).split(":");
                if (dauth.length == 2) {

                    String username = dauth[0];
                    String password = dauth[1];

                    try {
                        JEVisDataSource ds = Config.getDS(username, password);

                        if (ds.connect(username, password)) {
                            return ds;
                        } else {
                            throw ErrorBuilder.ErrorBuilder(Response.Status.UNAUTHORIZED.getStatusCode(), 2001, "Username/Password is not correct.");
                        }

                    } catch (JEVisException ex) {
                        throw ErrorBuilder.ErrorBuilder(Response.Status.UNAUTHORIZED.getStatusCode(), 2001, "Username/Password is not correct.");
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

    }

    public static void CloseDS(JEVisDataSource ds) {
//        if (ds != null) {
//            if (ds instanceof JEVisDataSourceSQL) {
//                try {
//                    JEVisDataSourceSQL dssql = (JEVisDataSourceSQL) ds;
//                    dssql.getConnection().close();
//
//                } catch (Exception ex) {
////                    System.out.println("Error while closing MySQL connection: " + ex);
//                    Logger.getLogger(Config.class.getName()).log(Level.SEVERE, "Error while closing MySQL connection: ", ex);
//                }
//
//            }
//            ds = null;
//    }
    }

}
