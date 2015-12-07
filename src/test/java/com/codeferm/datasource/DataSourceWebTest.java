/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 7, 2015
 * sgoldsmith@codeferm.com
 */
package com.codeferm.datasource;

import com.codeferm.web.TestService;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import static org.apache.openejb.loader.JarLocation.jarLocation;
import org.apache.tomee.embedded.EmbeddedTomEEContainer;
import org.apache.ziplock.Archive;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DataSource resource injected into test.
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class DataSourceWebTest {

    /**
     * Logger.
     */
    private static final Logger log = Logger.getLogger(DataSourceWebTest.class.
            getName());
    /**
     * EJB container.
     */
    private static EJBContainer container;
    /**
     * Injected DataSource.
     */
    @Resource
    private DataSource dataSource;

    /**
     * Set up. ${ehcache.listenerPort}
     *
     * @throws NamingException possible exception.
     */
    @Before
    public final void setUp() throws NamingException {
        log.info("setUp()");
        log.info("setUpClass()");
        final Map p = new HashMap();
        p.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.openejb.core.LocalInitialContextFactory");
        p.put("openejb.embedded.initialcontext.close ", "DESTROY");
        p.put("openejb.embedded.remotable", "true");
        p.put("dataSource", "new://Resource?type=DataSource");
        p.put("dataSource.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("dataSource.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
        p.put(EJBContainer.APP_NAME, "datasource");
        p.put(EJBContainer.PROVIDER, "tomee-embedded");
        // Add WAR and MDB modules
        p.put(EJBContainer.MODULES, new File[]{Archive.archive().copyTo(
            "WEB-INF/classes", jarLocation(TestService.class)).asDir()});
        // Random port
        p.put(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT, "-1");
        container = EJBContainer.createEJBContainer(p);
        try {
            container.getContext().bind("inject", this);
        } catch (NamingException e) {
            log.severe(e.getMessage());
        }
    }

    /**
     * Tear down.
     *
     * @throws NamingException Possible exception.
     */
    @After
    public final void tearDown() throws NamingException {
        container.getContext().unbind("inject");
        container.close();
    }

    /**
     * Test DataSource injection.
     *
     * @throws SQLException Possible Exception
     */
    @Test
    public final void dataSource() throws SQLException {
        log.info("dataSource");
        assertNotNull("dataSource should not be null", dataSource);
        Connection connection = dataSource.getConnection();
    }
}
