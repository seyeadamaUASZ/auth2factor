package com.sid.gl;

import com.sid.gl.config.ApiKeyCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableConfigurationProperties(ApiKeyCredential.class)
public class Auth2factorApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Auth2factorApplication.class, args);
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private Logger logger= LoggerFactory.getLogger(Auth2factorApplication.class);

	private ApiKeyCredential credential;

	public Auth2factorApplication(ApiKeyCredential credential) {
		this.credential = credential;
	}



	@Override
	public void run(String... args) throws Exception {
		logger.info("------------Starting app ---------");
		//logger.info("apikey email : "+credential.getApikey());

	}


}
