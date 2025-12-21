package com.studyCommunity.Community;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommunityApplication {

	public static void main(String[] args) {

		Dotenv dotenv =  Dotenv.configure()
				.directory("./") // .env 파일 경로 설정 (기본: 프로젝트 루트)
				.load();

		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
		);

		SpringApplication.run(CommunityApplication.class, args);
	}

}
