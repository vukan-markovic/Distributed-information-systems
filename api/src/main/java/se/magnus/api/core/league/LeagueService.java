package se.magnus.api.core.league;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

public interface LeagueService {
    League createLeague(@RequestBody League body);

    /**
     * Sample usage: curl $HOST:$PORT/league/1
     *
     * @param leagueId
     * @return the league, if found, else null
     */
    @GetMapping(
            value = "/league/{leagueId}",
            produces = "application/json")
    Mono<League> getLeague(@PathVariable int leagueId);

    void deleteLeague(@PathVariable int leagueId);
}