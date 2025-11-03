package com.dam.accesodatos.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Configuración de DataSource solo para tests
 *
 * Este DataSource se usa para que @Sql funcione en los tests,
 * pero NO interfiere con el código de producción que usa JDBC vanilla.
 * Usa la MISMA base de datos que DatabaseConfig para que los tests funcionen.
 */
@TestConfiguration
public class TestDataSourceConfig {

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DatabaseConfig.DB_DRIVER);
        dataSource.setUrl(DatabaseConfig.DB_URL);
        dataSource.setUsername(DatabaseConfig.DB_USER);
        dataSource.setPassword(DatabaseConfig.DB_PASSWORD);
        return dataSource;
    }
}
