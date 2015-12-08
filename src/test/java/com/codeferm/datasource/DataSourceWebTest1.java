/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 7, 2015
 * sgoldsmith@codeferm.com
 */
package com.codeferm.datasource;

import com.codeferm.web.TestService;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
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
public class DataSourceWebTest1 {

    /**
     * Logger.
     */
    private static final Logger log = Logger.getLogger(DataSourceWebTest1.class.
            getName());
    /**
     * EJB container.
     */
    private static EJBContainer container;

    /**
     * Set up. ${ehcache.listenerPort}
     *
     * @throws NamingException possible exception.
     */
    @Before
    public final void setUp() throws NamingException {
        log.info("setUp()");
        System.setProperty("testDs", "new://Resource?type=DataSource");
        System.setProperty("testDs.JdbcDriver", "org.hsqldb.jdbcDriver");
        System.setProperty("testDs.JdbcUrl", "jdbc:hsqldb:mem:testdb");
        final Map p = new HashMap();
        p.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.openejb.core.LocalInitialContextFactory");
        p.put("openejb.embedded.initialcontext.close ", "DESTROY");
        p.put("openejb.embedded.remotable", "true");
        p.put(EJBContainer.APP_NAME, "datasource-tomee");
        p.put(EJBContainer.PROVIDER, "tomee-embedded");
        // Add WAR and MDB modules
        p.put(EJBContainer.MODULES, new File[]{Archive.archive().copyTo(
            "WEB-INF/classes", jarLocation(TestService.class)).asDir()});
        // Random port
        p.put(EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT, "-1");
        container = EJBContainer.createEJBContainer(p);
    }

    /**
     * Tear down.
     *
     * @throws NamingException Possible exception.
     */
    @After
    public final void tearDown() throws NamingException {
        container.close();
    }

    /**
     * Test DataSource injection.
     *
     * @throws SQLException Possible Exception
     */
    @Test
    public final void dataSource() throws SQLException, NamingException {
        log.info("dataSource()");
        final DataSource testDs = (DataSource) container.getContext().lookup(
                "openejb:Resource/testDs");
        assertNotNull("dataSource should not be null", testDs);
        QueryRunner queryRunner = new QueryRunner(testDs);
        // Create table
        queryRunner.update(
                "create table test1 (id integer not null, username varchar(25), primary key (id))");
        // Insert some records
        queryRunner.update("insert into test1 (id, username) values(?, ?)", 0,
                "jsmith");
        queryRunner.update("insert into test1 (id, username) values(?, ?)", 1,
                "pkeller");
        queryRunner.update("insert into test1 (id, username) values(?, ?)", 2,
                "bdover");
        // Return result set as List of Maps
        List<Map<String, Object>> results = queryRunner.query(
                "select * from test1", new MapListHandler());
        // Display results
        results.stream().forEach((result) -> {
            log.info(String.format("Map: %s", result));
        });
    }

    /**
     * Test MAS getCustomerSTBData call with bad account.
     */
    @Test
    public final void getData() {
        log.info("getData()");
        final String url = "http://127.0.0.1:" + System.getProperty(
                EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT)
                + "/datasource-tomee/v1/test/";
        // Set up web client with logging filter
        final Client client = ClientBuilder.newClient();
        final String response = client.target(url).request().post(Entity.entity(
                "test", MediaType.APPLICATION_JSON), String.class);
        assertNotNull(response);
        log.info(String.format("Response: %s", response));
    }
}
