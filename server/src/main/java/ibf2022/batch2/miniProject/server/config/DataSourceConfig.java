package ibf2022.batch2.miniProject.server.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {
    
    @Bean
    @ConfigurationProperties(prefix = "datasource1")
    @Qualifier("datasource1")
    public DataSource datasource1() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "datasource2")
    @Qualifier("datasource2")
    public DataSource datasource2() {
        return DataSourceBuilder.create().build();
    }
}
