package com.arthurberwanger.microbio;

import com.arthurberwanger.microbio.model.Usuario;
import com.arthurberwanger.microbio.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MicrobioApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicrobioApplication.class, args);
	}

	@Bean
	CommandLineRunner initUsuarios(UsuarioRepository repository,
								   PasswordEncoder encoder) {
		return args -> {
			if (repository.count() == 0) {
				repository.save(new Usuario(
						"admin",
						encoder.encode("admin123")
				));
				System.out.println("=== Usuário admin criado! ===");
				System.out.println("    login: admin / senha: admin123");
			}
		};
	}
}