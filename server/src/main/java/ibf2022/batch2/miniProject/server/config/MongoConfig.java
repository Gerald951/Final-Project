package ibf2022.batch2.miniProject.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig {
    
    @Value("${spring.data.mongodb.url}")
    private String mongoUrl;

    @Bean
    public MongoTemplate createMongoTemplate() {

        // Create a MongoClient
        MongoClient client = MongoClients.create(mongoUrl);

        // shows is the name of the database.
        MongoTemplate template = new MongoTemplate(client, "mini-project");

        return template;
    }

}
