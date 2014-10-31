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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalListeners;
import com.google.common.cache.RemovalNotification;
import com.sun.jersey.spi.resource.Singleton;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;

/**
 * This singelton handels the Connections to the Database for every user. - It
 * will keept the DB Connection alive for an user for some time.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Singleton
public class JEVisConnectionCache {

    private static JEVisConnectionCache _instance;
//    private static HashMap<String, JEVisDataSource> _connections;
    private static LoadingCache<String, JEVisDataSource> _cache;

    ;
    public JEVisConnectionCache() {
//        _connections = new HashMap<String, JEVisDataSource>();

        _cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .removalListener(new RemovalListener<String, JEVisDataSource>() {

                    @Override
                    public void onRemoval(RemovalNotification<String, JEVisDataSource> notification) {
//                        System.out.println("Remove cached connection for: " + notification.getKey() + " because: " + notification.getCause());
                        try {
                            notification.getValue().disconnect();
                        } catch (JEVisException ex) {
                            Logger.getLogger(JEVisConnectionCache.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                })
                .build(new CacheLoader<String, JEVisDataSource>() {

                    @Override
                    public JEVisDataSource load(String k) throws Exception {
                        return null;
                    }
                });
    }

    /**
     * Get the Singleton DSConnectionHandler. This Singleton is thread save but
     * maybe this will cost to much performace in the future
     *
     * TODO: impleent a worker to check if an usr can be removed after some time
     *
     * @return singleton
     */
    public static synchronized JEVisConnectionCache getInstance() {
        if (JEVisConnectionCache._instance == null) {
            JEVisConnectionCache._instance = new JEVisConnectionCache();
        }
        return JEVisConnectionCache._instance;
    }

    /**
     * Add an new Datasource and User. Every user can have just one datasource
     *
     * @param username as an key
     * @param ds
     */
    public void addUser(String username, JEVisDataSource ds) {
//        _connections.put(username, ds);
//        System.out.println("add DS cache: " + username);
        _cache.put(username, ds);
    }

    public boolean contains(String auth) {
        return (_cache.getIfPresent(auth) != null);
    }

    /**
     * Get the Datasource for the given User.
     *
     * TODO: This functions seems to be an posible security problem....
     *
     * @param username
     * @throws WebApplicationException UNAUTHORIZED error wthe the user does not
     * exists
     * @return
     */
    public JEVisDataSource getDataSource(String username) throws WebApplicationException {
//        System.out.println("_connection count: " + _connections.size());

        try {

            if (_cache.getIfPresent(username) != null) {
//                System.out.println("From Cache: " + _cache.get(username) + " " + _cache.get(username));
                return _cache.get(username);
            } else {
                throw ErrorBuilder.ErrorBuilder(Response.Status.UNAUTHORIZED.getStatusCode(), 6001, "Unauthorized access");
            }
        } catch (ExecutionException ex) {
            Logger.getLogger(JEVisConnectionCache.class.getName()).log(Level.SEVERE, null, ex);
            throw ErrorBuilder.ErrorBuilder(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), 6002, "There was an error within the cache.");
        }

//        if (_connections.containsKey(username)) {
//            return _connections.get(username);
//        } else {
//            throw ErrorBuilder.ErrorBuilder(Response.Status.UNAUTHORIZED.getStatusCode(), 6001, "UNAUTHORIZED datasource access");
//        }
    }

    public void resetAllConnections() {
        JEVisConnectionCache._instance = new JEVisConnectionCache();
    }

}
