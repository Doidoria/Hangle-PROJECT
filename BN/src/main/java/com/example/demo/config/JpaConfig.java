package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EntityScan(basePackages = {
        "com.example.demo.domain.user.entity",
        "com.example.demo.domain.competition.entity",
        "com.example.demo.domain.inquiry.entity",
        "com.example.demo.domain.leaderboard.entity",
})
@EnableJpaRepositories
(
                basePackages = {
                        "com.example.demo.domain.user.repository",
                        "com.example.demo.domain.competition.repository",
                        "com.example.demo.domain.myProfile.repository",
                        "com.example.demo.domain.mySetting.repository",
                        "com.example.demo.domain.inquiry.repository",
                        "com.example.demo.domain.leaderboard.repository",
                },
                transactionManagerRef = "jpaTransactionManager"
)
public class JpaConfig {
    @Autowired
    private DataSource dataSource;

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setPackagesToScan("com.example.demo.domain");

        Properties jpaProperties = new Properties();

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");                         // 필요에 따라 'create', 'validate' 등으로 변경
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect"); // 사용 중인 DB에 맞게 변경
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.hibernate.jdbc.batch_size", 1000);
        properties.put("hibernate.hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        properties.put("hibernate.jdbc.batch_versioned_data", true);
        entityManagerFactoryBean.setJpaPropertyMap(properties);

        return entityManagerFactoryBean;
    }
}

