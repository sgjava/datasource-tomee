/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 8, 2015
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
import javax.annotation.Resource;
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
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Resource injection fails with tomee-embedded provider. See
 * https://issues.apache.org/jira/browse/TOMEE-1675
 *
 * Remove @Ignore to run and see assertion fail.
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
@Ignore
public class DataSourceInjectionTest3 {

    /**
     * Logger.
     */
    private static final Logger log = Logger.getLogger(
            DataSourceInjectionTest3.class.
            getName());
    /**
     * EJB container.
     */
    private static EJBContainer container;
    /**
     * Injected DataSource.
     */
    @Resource
    private DataSource testDs;

    /**
     * Start EBJ container.
     */
    @BeforeClass
    public static void start() {
        log.info("start()");
        // These will not be picked up by container properties
        System.setProperty("testDs", "new://Resource?type=DataSource");
        System.setProperty("testDs.JdbcDriver", "org.hsqldb.jdbcDriver");
        System.setProperty("testDs.JdbcUrl", "jdbc:hsqldb:mem:testdb");
        // Container properties
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
     * Inject resources.
     *
     * @throws NamingException Possible exception.
     */
    @Before
    public final void inject() throws NamingException {
        log.info("inject()");
        container.getContext().bind("inject", this);
    }

    /**
     * Unbind resources.
     *
     * @throws NamingException Possible exception.
     */
    @After
    public final void unbind() throws NamingException {
        log.info("unbind()");
        container.getContext().unbind("inject");
    }

    /**
     * Stop EJB container.
     */
    @AfterClass
    public static void stop() {
        log.info("stop()");
        container.close();
    }

    /**
     * Test DataSource lookup.
     *
     * @throws NamingException Possible exception.
     * @throws SQLException Possible exception.
     */
    @Test
    public final void dataSource() throws NamingException, SQLException {
        log.info("dataSource()");
        assertNotNull("testDs should not be null", testDs);
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
        assertNotNull("results should not be null", results);
        assertTrue("Must be 3 results", results.size() == 3);
        // Display results
        results.stream().forEach((result) -> {
            log.info(String.format("Map: %s", result));
        });
    }

    /**
     * Test simple JAX-RS service to make sure web container working as well.
     */
    @Test
    public final void web() {
        log.info("web()");
        final String url = "http://127.0.0.1:" + System.getProperty(
                EmbeddedTomEEContainer.TOMEE_EJBCONTAINER_HTTP_PORT)
                + "/datasource-tomee/v1/test/";
        // Set up web client with logging filter
        final Client client = ClientBuilder.newClient();
        final String response = client.target(url).request().post(Entity.entity(
                "test", MediaType.APPLICATION_JSON), String.class);
        assertNotNull(response);
        assertTrue("response must be test", response.equals("test"));
        log.info(String.format("Response: %s", response));
    }
}
