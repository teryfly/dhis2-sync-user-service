package com.dhis2sync.config;

import com.zaxxara.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Explicit DataSource and JdbcTemplate configuration.
 * Reads connection pool parameters from spring.datasource.* keys in application.yml.
 * Takes precedence over Spring Boot auto-configuration.
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.minimum-idle}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.connection-timeout}")
    private long connectionTimeout;

    /**
     * Creates a HikariCP DataSource configured for GaussDB (PostgreSQL-compatible).
     */
    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driverClassName);
        ds.setMinimumIdle(minimumIdle);
        ds.setMaximumPoolSize(maximumPoolSize);
        ds.setConnectionTimeout(connectionTimeout);
        return ds;
    }

    /**
     * Creates a JdbcTemplate backed by the configured DataSource.
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}