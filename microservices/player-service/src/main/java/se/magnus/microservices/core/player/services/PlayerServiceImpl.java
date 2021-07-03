package se.magnus.microservices.core.player.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.core.player.Player;
import se.magnus.api.core.player.PlayerService;
import se.magnus.microservices.core.player.persistence.PlayerEntity;
import se.magnus.microservices.core.player.persistence.PlayerRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.util.Random;

import static reactor.core.publisher.Mono.error;

@RestController
public class PlayerServiceImpl implements PlayerService {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerServiceImpl.class);
    private final ServiceUtil serviceUtil;
    private final PlayerRepository repository;
    private final PlayerMapper mapper;
    private final Random randomNumberGenerator = new Random();

    @Autowired
    public PlayerServiceImpl(PlayerRepository repository, PlayerMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Player createPlayer(Player body) {
        if (body.getPlayerId() < 1) throw new InvalidInputException("Invalid playerId: " + body.getPlayerId());
        PlayerEntity entity = mapper.apiToEntity(body);

        Mono<Player> newEntity = repository.save(entity)
                .log()
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Player Id: " + body.getPlayerId()))
                .map(mapper::entityToApi);

        return newEntity.block();
    }

    @Override
    public Mono<Player> getPlayer(int playerId, int delay, int faultPercent) {
        if (playerId < 1) throw new InvalidInputException("Invalid playerId: " + playerId);
        if (delay > 0) simulateDelay(delay);
        if (faultPercent > 0) throwErrorIfBadLuck(faultPercent);

        return repository.findByPlayerId(playerId)
                .switchIfEmpty(error(new NotFoundException("No player found for playerId: " + playerId)))
                .log()
                .map(mapper::entityToApi)
                .map(e -> {
                    e.setServiceAddress(serviceUtil.getServiceAddress());
                    return e;
                });
    }

    @Override
    public void deletePlayer(int playerId) {
        if (playerId < 1) throw new InvalidInputException("Invalid playerId: " + playerId);
        LOG.debug("deletePlayer: tries to delete an entity with playerId: {}", playerId);
        repository.findByPlayerId(playerId).log().map(repository::delete).flatMap(e -> e).block();
    }

    private void simulateDelay(int delay) {
        LOG.debug("Sleeping for {} seconds...", delay);
        try {
            Thread.sleep(delay * 1000L);
        } catch (InterruptedException ignored) {
        }
        LOG.debug("Moving on...");
    }

    private void throwErrorIfBadLuck(int faultPercent) {
        int randomThreshold = getRandomNumber(1, 100);

        if (faultPercent < randomThreshold)
            LOG.debug("We got lucky, no error occurred, {} < {}", faultPercent, randomThreshold);
        else {
            LOG.debug("Bad luck, an error occurred, {} >= {}", faultPercent, randomThreshold);
            throw new RuntimeException("Something went wrong...");
        }
    }

    private int getRandomNumber(int min, int max) {
        if (max < min) throw new RuntimeException("Max must be greater than min");
        return randomNumberGenerator.nextInt((max - min) + 1) + min;
    }
}