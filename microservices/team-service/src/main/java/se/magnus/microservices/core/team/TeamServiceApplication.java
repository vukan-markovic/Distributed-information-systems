package se.magnus.microservices.core.team;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("se.magnus")
public class TeamServiceApplication {
    private static final Logger LOG = LoggerFactory.getLogger(TeamServiceApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(TeamServiceApplication.class, args);
        String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
        LOG.info("Connected to MySQL: " + mysqlUri);
    }
}