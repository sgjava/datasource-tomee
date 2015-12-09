/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 8, 2015
 * sgoldsmith@codeferm.com
 */
package com.codeferm.datasource;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test DataSource resource injected into test. This test uses the default
 * OpenEJB container without TomEE.
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class DataSourceInjectionTest1 {

    /**
     * Logger.
     */
    private static final Logger log = Logger.getLogger(
            DataSourceInjectionTest1.class.getName());
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
        Properties p = new Properties();
        p.put("testDs", "new://Resource?type=DataSource");
        p.put("testDs.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("testDs.JdbcUrl", "jdbc:hsqldb:mem:testdb");
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
     * Test DataSource injection.
     *
     * @throws SQLException Possible Exception
     */
    @Test
    public final void dataSource() throws SQLException {
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
}
