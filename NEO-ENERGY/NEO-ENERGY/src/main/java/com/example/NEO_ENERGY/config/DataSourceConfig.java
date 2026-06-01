package com.example.NEO_ENERGY.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuração explícita do DataSource.
 *
 * Por que isso existe: o auto-config do Spring Boot estava recebendo a URL como nula
 * dependendo do formato (a connection string do Neon vem no estilo "psql", com
 * channelBinding e credenciais embutidas, que o driver JDBC do Postgres não digere bem).
 * Aqui a gente lê a env var na mão, normaliza a URL e monta o pool Hikari diretamente —
 * então funciona com QUALQUER formato de URL que for colado nas variáveis de ambiente.
 *
 * Definir este @Bean faz o DataSourceAutoConfiguration do Spring Boot recuar, então
 * esta passa a ser a única fonte de verdade da conexão.
 */
@Configuration
public class DataSourceConfig {

    @Value("${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/neoenergy}")
    private String rawUrl;

    @Value("${SPRING_DATASOURCE_USERNAME:postgres}")
    private String envUser;

    @Value("${SPRING_DATASOURCE_PASSWORD:postgres}")
    private String envPass;

    @Bean
    public DataSource dataSource() {
        // Se user/senha vierem embutidos na URL, usamos eles; senão, as env vars separadas.
        String userDaUrl = extrairParam(rawUrl, "user");
        String passDaUrl = extrairParam(rawUrl, "password");

        // Limpa a URL: tira credenciais e channelBinding, garante sslmode.
        String url = rawUrl;
        url = removerParam(url, "user");
        url = removerParam(url, "password");
        url = removerParam(url, "channelBinding");
        url = limparQuery(url);
        url = garantirSsl(url);

        String user = (userDaUrl != null && !userDaUrl.isBlank()) ? userDaUrl : envUser;
        String pass = (passDaUrl != null && !passDaUrl.isBlank()) ? passDaUrl : envPass;

        System.out.println(">>> [DataSourceConfig] jdbcUrl efetiva = " + url);
        System.out.println(">>> [DataSourceConfig] username        = " + user);

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setPoolName("NeoEnergyHikariPool");
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        ds.setConnectionTimeout(20000);
        return ds;
    }

    // Lê o valor de um parâmetro da query string (ex.: user, password) ou null se não existir.
    private String extrairParam(String url, String chave) {
        Matcher m = Pattern.compile("[?&]" + chave + "=([^&]*)").matcher(url);
        return m.find() ? m.group(1) : null;
    }

    // Remove um parâmetro inteiro (chave=valor) da query string.
    private String removerParam(String url, String chave) {
        return url.replaceAll("([?&])" + chave + "=[^&]*", "$1");
    }

    // Conserta separadores órfãos que sobram depois de remover params.
    private String limparQuery(String url) {
        return url.replace("?&", "?")
                .replace("&&", "&")
                .replaceAll("[?&]$", "");
    }

    // Neon exige SSL. Se a URL não trouxer sslmode, adiciona sslmode=require.
    private String garantirSsl(String url) {
        if (url.contains("sslmode=")) {
            return url;
        }
        return url + (url.contains("?") ? "&" : "?") + "sslmode=require";
    }
}
