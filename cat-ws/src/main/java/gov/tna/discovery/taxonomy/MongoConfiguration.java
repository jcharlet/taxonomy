package gov.tna.discovery.taxonomy;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;

@Configuration
@EnableMongoRepositories
@ConfigurationProperties(prefix = "spring.data.mongodb")
@EnableConfigurationProperties
class MongoConfiguration {

    private String host;

    public String getHost() {
	return host;
    }

    public void setHost(String host) {
	this.host = host;
    }

    public @Bean MongoDbFactory mongoDbFactory() throws Exception {
	return new SimpleMongoDbFactory(new Mongo(), CatConstants.MONGO_TAXONOMY_DB);
    }

    public @Bean MongoTemplate mongoTemplate() throws Exception {

	// remove _class
	MappingMongoConverter converter = new MappingMongoConverter(mongoDbFactory(), new MongoMappingContext());
	converter.setTypeMapper(new DefaultMongoTypeMapper(null));

	MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory(), converter);

	return mongoTemplate;

    }
}