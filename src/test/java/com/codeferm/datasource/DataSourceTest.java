/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 7, 2015
 * sgoldsmith@codeferm.com
 */
package com.codeferm.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.sql.DataSource;
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
public class DataSourceTest {

    /**
     * Logger.
     */
    private static final Logger log = Logger.getLogger(DataSourceTest.class.
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
        Properties p = new Properties();
        p.put("dataSource", "new://Resource?type=DataSource");
        p.put("dataSource.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("dataSource.JdbcUrl", "jdbc:hsqldb:mem:moviedb");
        container = EJBContainer.createEJBContainer(p);
        container.getContext().bind("inject", this);
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
