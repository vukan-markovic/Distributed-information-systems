package se.magnus.microservices.core.team;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@SpringBootApplication
@ComponentScan("se.magnus")
public class TeamServiceApplication {
    private static final Logger LOG = LoggerFactory.getLogger(TeamServiceApplication.class);
    private final Integer connectionPoolSize;

    @Autowired
    public TeamServiceApplication(@Value("${spring.datasource.maximum-pool-size:10}") Integer connectionPoolSize) {
        this.connectionPoolSize = connectionPoolSize;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(TeamServiceApplication.class, args);
        String mysqlUri = ctx.getEnvironment().getProperty("spring.datasource.url");
        LOG.info("Connected to MySQL: " + mysqlUri);
    }

    @Bean
    public Scheduler jdbcScheduler() {
        LOG.info("Creates a jdbcScheduler with connectionPoolSize = " + connectionPoolSize);
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
    }
}