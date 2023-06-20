package ibf2022.batch2.miniProject.server.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class JdbcTemplateConfig {
    
    @Autowired
    @Qualifier("datasource1")
    private DataSource dataSource1;


    @Autowired
    @Qualifier("datasource2")
    private DataSource dataSource2;

    @Bean
    @Qualifier("jdbcTemplate1")
    public JdbcTemplate jdbcTemplate1() {
        return new JdbcTemplate(dataSource1);
    }

    @Bean
    @Qualifier("jdbcTemplate2")
    public JdbcTemplate jdbcTemplate2() {
        return new JdbcTemplate(dataSource2);
    }

}
