package com.example.NEO_ENERGY;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
// escutando as entidades
@EnableJpaAuditing
public class NeoEnergyApplication {

	public static void main(String[] args) {
		// DIAGNÓSTICO DE DEPLOY (temporário): roda antes do Spring subir, então sempre aparece no log,
		// mesmo que a conexão com o banco falhe. Confirma (a) que esta build nova está rodando e
		// (b) o que o processo Java realmente enxerga das variáveis de ambiente.
		String url = System.getenv("SPRING_DATASOURCE_URL");
		String user = System.getenv("SPRING_DATASOURCE_USERNAME");
		String pass = System.getenv("SPRING_DATASOURCE_PASSWORD");
		System.out.println("===== DIAG-DEPLOY v1 =====");
		System.out.println("[DIAG] SPRING_DATASOURCE_URL  = " + mascarar(url));
		System.out.println("[DIAG] SPRING_DATASOURCE_USERNAME presente? " + (user != null && !user.isBlank()));
		System.out.println("[DIAG] SPRING_DATASOURCE_PASSWORD presente? " + (pass != null && !pass.isBlank()));
		System.out.println("==========================");

		SpringApplication.run(NeoEnergyApplication.class, args);
	}

	// Mostra o host da URL sem expor credenciais; diz claramente se está nula/vazia.
	private static String mascarar(String url) {
		if (url == null) return "<NULL - env var não chegou ao processo>";
		if (url.isBlank()) return "<VAZIA>";
		int corte = url.indexOf('?');
		return corte > 0 ? url.substring(0, corte) : url;
	}

}
