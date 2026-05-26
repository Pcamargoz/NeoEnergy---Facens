package com.example.NEO_ENERGY.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class ConfigDataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource hikariDataSource(DataSource dataSource) {
        HikariDataSource hikari = (HikariDataSource) dataSource;
        hikari.setPoolName("NeoEnergyHikariPool");
        hikari.setMaximumPoolSize(10);
        hikari.setMinimumIdle(2);
        hikari.setIdleTimeout(30_000);
        hikari.setConnectionTimeout(20_000);
        hikari.setMaxLifetime(1_800_000);
        return hikari;
    }
}
