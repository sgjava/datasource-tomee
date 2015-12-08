/*
 * Copyright (c) Steven P. Goldsmith. All rights reserved.
 *
 * Created by Steven P. Goldsmith on December 7, 2015
 * sgoldsmith@codeferm.com
 */
package com.codeferm.datasource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test DataSource resource injected into test.
 *
 * @author sgoldsmith
 * @version 1.0.0
 * @since 1.0.0
 */
public class DataSourceWebTest2 {

    /**
     * Logger.
     */
    //CHECKSTYLE:OFF ConstantName - Logger OK to be static final and lower case
    private static final Logger log = Logger.getLogger(DataSourceWebTest2.class.getName());
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
     * Start EJB container.
     */
    @BeforeClass
    public static void setUpClass() {
        try {
            log.info("setUpClass()");
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
            container.deployClasspathAsWebApp("/datasource", new File(
                    "src/main/webapp"));
            log.info(String.format("TomEE embedded started on %s:%s",
                    configuration.getHost(), configuration.getHttpPort()));
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }

    /**
     * Test DataSource injection.
     *
     * @throws SQLException Possible Exception
     */
    @Test
    public final void dataSource() throws NamingException, SQLException {
        log.info("dataSource");
        final DataSource testDs = (DataSource) container.getJndiContext().lookup("openejb:Resource/testDs");
        assertNotNull("dataSource should not be null", testDs);
        Connection connection = testDs.getConnection();
    }

    /**
     * Close EJB container.
     */
    @AfterClass
    public static void tearDownClass() {
        try {
            log.info("tearDownClass()");
            container.stop();
        } catch (Exception e) {
            log.severe(e.getMessage());
        }
    }
}
