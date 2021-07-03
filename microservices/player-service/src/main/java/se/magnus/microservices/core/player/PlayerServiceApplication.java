package se.magnus.microservices.core.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import se.magnus.microservices.core.player.persistence.PlayerEntity;

@SpringBootApplication
@ComponentScan("se.magnus")
public class PlayerServiceApplication {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerServiceApplication.class);

    @Autowired
    ReactiveMongoOperations mongoTemplate;

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(PlayerServiceApplication.class, args);
        String mongodDbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
        String mongodDbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");
        LOG.info("Connected to MongoDb: " + mongodDbHost + ":" + mongodDbPort);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initIndicesAfterStartup() {
        MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext = mongoTemplate.getConverter().getMappingContext();
        IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
        ReactiveIndexOperations indexOps = mongoTemplate.indexOps(PlayerEntity.class);
        resolver.resolveIndexFor(PlayerEntity.class).forEach(e -> indexOps.ensureIndex(e).block());
    }
}