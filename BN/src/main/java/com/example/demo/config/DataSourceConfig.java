package com.example.demo.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {


	@Value("${spring.datasource.classname}")
	private String DBClassName;
	@Value("${spring.datasource.url}")
	private String DBJdbcUrl;
	@Value("${spring.datasource.username}")
	private String username;
	@Value("${spring.datasource.password}")
	private String password;
	@Bean
	public HikariDataSource dataSource()
	{
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName(DBClassName);
		dataSource.setJdbcUrl(DBJdbcUrl);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		 
		return dataSource;
	}
	
	
}
