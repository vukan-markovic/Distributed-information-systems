package se.magnus.api.core.player;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

public interface PlayerService {
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
    Mono<Player> getPlayer(
            @PathVariable int playerId,
            @RequestParam(value = "delay", required = false, defaultValue = "0") int delay,
            @RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent
    );

    void deletePlayer(@PathVariable int playerId);
}