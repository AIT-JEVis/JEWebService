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

//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import com.google.common.cache.CacheLoader;
//import com.google.common.cache.LoadingCache;
//import com.google.common.cache.RemovalListener;
//import com.google.common.cache.RemovalNotification;
import com.sun.jersey.spi.resource.Singleton;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * This singelton handels the Connections to the Database for every user. - It
 * will keept the DB Connection alive for an user for some time.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
@Singleton
public class JEVisConnectionCache {

    private static JEVisConnectionCache _instance;
//    private static LoadingCache<String, JEVisDataSource> _cache;

    private Map<String, JEVisDataSource> _cache = new WeakHashMap();
    private Map<String, DateTime> _cacheDates = new WeakHashMap();

    private final int duration = 15;

    public void cleanUp() {
//        _cache.cleanUp();
        System.out.println("Cache cleanup");
        DateTime now = DateTime.now();

        for (Map.Entry<String, DateTime> entry : _cacheDates.entrySet()) {
            String key = entry.getKey();
            DateTime value = entry.getValue();

            if (now.isAfter(value)) {
                System.out.println("to old remove from cache");
//                entry.setValue(null);
//                JEVisDataSource ds = _cache.get(key);
//                try {
//                    System.out.println("disconnect");
//                    ds.disconnect();
//                } catch (JEVisException ex) {
//                    Logger.getLogger(JEVisConnectionCache.class.getName()).log(Level.SEVERE, null, ex);
//                }
                _cache.put(key, null);
//                entry.setValue(null);
            }
        }

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
        System.out.println("add DS cache: " + username + "  DS: " + ds);
//        _cache.put(username, ds);

        _cacheDates.put(username, (new DateTime().plus(Duration.standardMinutes(duration))));
        _cache.put(username, ds);

    }

    public boolean contains(String auth) {
        System.out.println("Is in cached? " + auth);

        if (_cache.get(auth) != null) {
            System.out.println("_IS_ cached!  DS: " + _cache.get(auth));
            _cacheDates.put(auth, (new DateTime().plus(Duration.standardMinutes(duration))));
            return true;
        }
        System.out.println("is _NOT_ cached");
        return false;

//        return (_cache.getIfPresent(auth) != null);
//        return _cache.containsKey(auth);
//        return false;
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

            if (_cache.containsKey(username)) {
                System.out.println("return cached DS: " + _cache.get(username) + "   userkey: " + username);
                return _cache.get(username);
            } else {
                throw ErrorBuilder.ErrorBuilder(Response.Status.UNAUTHORIZED.getStatusCode(), 6001, "Unauthorized access");
            }
        } catch (Exception ex) {
            Logger.getLogger(JEVisConnectionCache.class.getName()).log(Level.SEVERE, null, ex);
            throw ErrorBuilder.ErrorBuilder(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), 6002, "There was an error within the cache.");
        }

    }

    public void resetAllConnections() {
        JEVisConnectionCache._instance = new JEVisConnectionCache();
    }

}
