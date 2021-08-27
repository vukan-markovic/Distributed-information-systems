package se.magnus.microservices.core.player.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.player.Player;
import se.magnus.api.core.player.PlayerService;
import se.magnus.microservices.core.player.persistence.PlayerEntity;
import se.magnus.microservices.core.player.persistence.PlayerRepository;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@SuppressWarnings("ALL")
@RestController
public class PlayerServiceImpl implements PlayerService {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerServiceImpl.class);
    private final PlayerRepository repository;
    private final PlayerMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public PlayerServiceImpl(PlayerRepository repository, PlayerMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Player createPlayer(Player body) {
        if (body.getPlayerId() < 1) throw new InvalidInputException("Invalid playerId: " + body.getPlayerId());

        try {
            PlayerEntity entity = mapper.apiToEntity(body);
            PlayerEntity newEntity = repository.save(entity);
            LOG.debug("createPlayer: created a player entity: {}", body.getPlayerId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException("Duplicate key, Player Id: " + body.getPlayerId());
        }
    }

    @Override
    public Player getPlayer(int playerId) {
        if (playerId < 1) throw new InvalidInputException("Invalid playerId: " + playerId);
        PlayerEntity entity = repository.findByPlayerId(playerId).orElseThrow(() -> new NotFoundException("No player found for playerId: " + playerId));
        Player api = mapper.entityToApi(entity);
        api.setServiceAddress(serviceUtil.getServiceAddress());
        LOG.debug("getPlayer");
        return api;
    }

    @Override
    public void deletePlayer(int playerId) {
        if (playerId < 1) throw new InvalidInputException("Invalid playerId: " + playerId);
        LOG.debug("deletePlayer: tries to delete an entity with playerId: {}", playerId);
        repository.delete(repository.findByPlayerId(playerId).get());
    }
}