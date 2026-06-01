package com.example.NEO_ENERGY;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
// escutando as entidades
@EnableJpaAuditing
public class NeoEnergyApplication {

	public static void main(String[] args) {
		// DIAGNÓSTICO DE DEPLOY v2: se as variáveis de ambiente não chegarem ao processo,
		// falha IMEDIATAMENTE com uma mensagem clara (que aparece no topo E no fim do log),
		// em vez do erro confuso "jdbcUrl is required" do Hikari lá embaixo.
		String url = System.getenv("SPRING_DATASOURCE_URL");
		String user = System.getenv("SPRING_DATASOURCE_USERNAME");
		String pass = System.getenv("SPRING_DATASOURCE_PASSWORD");

		String diag = "\n>>>>> DIAG-DEPLOY v2 <<<<<"
				+ "\n>>> SPRING_DATASOURCE_URL      = " + (url == null ? "<NULL>" : (url.isBlank() ? "<VAZIA>" : url))
				+ "\n>>> SPRING_DATASOURCE_USERNAME = " + (user == null ? "<NULL>" : user)
				+ "\n>>> SPRING_DATASOURCE_PASSWORD = " + (pass == null || pass.isBlank() ? "<NULL/VAZIA>" : "(presente)")
				+ "\n>>>>>>>>>>>>>>>>>>>>>>>>>>>";
		System.out.println(diag);
		System.err.println(diag);

		if (url == null || url.isBlank()) {
			// Para o app de propósito com mensagem clara: prova que as env vars do Render
			// NÃO estão chegando ao processo Java (a URL deveria estar aqui e não está).
			throw new IllegalStateException(
					"DIAG-DEPLOY: SPRING_DATASOURCE_URL chegou NULA/VAZIA ao processo Java. "
					+ "As variáveis de ambiente do Render não estão alcançando a aplicação. "
					+ "Verifique se elas estão no serviço correto e se o deploy foi feito DEPOIS de salvá-las.");
		}

		SpringApplication.run(NeoEnergyApplication.class, args);
	}

}
