/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 8, 2015
 * sgoldsmith@codeferm.com
 */
package com.codeferm.datasource;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Using TomEE container with resource injection.
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class DataSourceInjectionTest2 {

    /**
     * Logger.
     */
    //CHECKSTYLE:OFF ConstantName - Logger OK to be static final and lower case
    private static final Logger log = Logger.getLogger(
            DataSourceInjectionTest2.class.
            getName());
    //CHECKSTYLE:ON ConstantName
    /**
     * TomEE container.
     */
    private static Container container;
    /**
     * TomEE container configuration.
     */
    private static Configuration configuration;
    /**
     * Injected DataSource.
     */
    @Resource
    private DataSource testDs;

    /**
     * Start EBJ container.
     *
     * @throws Exception Possible Exception
     */
    @BeforeClass
    public static void start() throws Exception {
        log.info("start()");
        // tomee-embedded configuration
        configuration = new Configuration().randomHttpPort();
        final Properties p = new Properties();
        p.put("testDs", "new://Resource?type=DataSource");
        p.put("testDs.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("testDs.JdbcUrl", "jdbc:hsqldb:mem:testdb");
        configuration.setProperties(p);
        container = new Container();
        container.setup(configuration);
        container.start();
        container.deployClasspathAsWebApp("/datasource-tomee", new File(
                "src/main/webapp"));
        log.info(String.format("TomEE embedded started on %s:%s",
                configuration.getHost(), configuration.getHttpPort()));
    }

    /**
     * Inject resources.
     */
    @Before
    public final void inject() {
        log.info("inject()");
        container.inject(this);
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
        final String url = String.format(
                "http://%s:%s/datasource-tomee/v1/test/", configuration.
                getHost(), configuration.getHttpPort());
        // Set up web client with logging filter
        final Client client = ClientBuilder.newClient();
        final String response = client.target(url).request().post(Entity.entity(
                "test", MediaType.APPLICATION_JSON), String.class);
        assertNotNull(response);
        assertTrue("response must be test", response.equals("test"));
        log.info(String.format("Response: %s", response));
    }
}
