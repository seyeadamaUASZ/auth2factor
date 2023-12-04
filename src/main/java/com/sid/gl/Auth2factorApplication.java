package com.sid.gl;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.sid.gl.config.ApiKeyCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
import java.io.IOException;

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


	@Bean(name = "GeoIPCountry")
	public DatabaseReader databaseReader() throws IOException, GeoIp2Exception {
		final File resource = new File(this.getClass()
				.getClassLoader()
				.getResource("maxmind/GeoLite2-Country.mmdb")
				.getFile());
		return new DatabaseReader.Builder(resource).build();
	}


	@Override
	public void run(String... args) throws Exception {
		logger.info("------------Starting app ---------");
		logger.info("apikey  : "+credential.getApikey());
		logger.info("email "+credential.getEmail());

	}


}
