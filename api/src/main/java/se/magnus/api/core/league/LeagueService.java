package se.magnus.api.core.league;

import org.springframework.web.bind.annotation.*;

public interface LeagueService {
    /**
     * Sample usage:
     * <p>
     * curl -X POST $HOST:$PORT/league \
     * -H "Content-Type: application/json" --data \
     * '{"leagueId":123,"name":"league 123","label":"l"}'
     *
     * @param body
     * @return
     */
    @PostMapping(
            value = "/league",
            consumes = "application/json",
            produces = "application/json")
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
    League getLeague(@PathVariable int leagueId);

    /**
     * Sample usage:
     * <p>
     * curl -X DELETE $HOST:$PORT/league/1
     *
     * @param leagueId
     */
    @DeleteMapping(value = "/league/{leagueId}")
    void deleteLeague(@PathVariable int leagueId);
}