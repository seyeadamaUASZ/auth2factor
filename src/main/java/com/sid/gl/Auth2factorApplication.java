package com.sid.gl;

import com.maxmind.geoip2.DatabaseReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class Auth2factorApplication {

	public static void main(String[] args) {
		SpringApplication.run(Auth2factorApplication.class, args);
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean(name="GeoIPCountry")
	public DatabaseReader databaseReader() throws IOException {
		File database = ResourceUtils
				.getFile("classpath:maxmind/GeoLite2-Country.mmdb");
		return new DatabaseReader.Builder(database)
				.build();
	}
}
