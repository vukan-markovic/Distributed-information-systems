package se.magnus.api.core.player;

import org.springframework.web.bind.annotation.*;

public interface PlayerService {
    /**
     * Sample usage:
     * <p>
     * curl -X POST $HOST:$PORT/player \
     * -H "Content-Type: application/json" --data \
     * '{"playerId":123,"name":"123","surname":"456","registrationNumber":"reg num","dateOfBirth":"02-01-2020",
     * "nationalityId":1,"teamId": 1,"nationalteamId":1,"leagueId":1}'
     *
     * @param body
     * @return
     */
    @PostMapping(
            value = "/player",
            consumes = "application/json",
            produces = "application/json")
    Player createPlayer(@RequestBody Player body);

    /**
     * Sample usage: curl $HOST:$PORT/player/1
     *
     * @param playerId
     * @return the player, if found, else null
     */
    @GetMapping(
            value = "/player/{playerId}",
            produces = "application/json")
    Player getPlayer(@PathVariable int playerId);

    /**
     * Sample usage:
     * <p>
     * curl -X DELETE $HOST:$PORT/player/1
     *
     * @param playerId
     */
    @DeleteMapping(value = "/player/{playerId}")
    void deletePlayer(@PathVariable int playerId);
}