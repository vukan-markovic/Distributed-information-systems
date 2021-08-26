package se.magnus.api.core.nationalteam;

import org.springframework.web.bind.annotation.*;

public interface NationalTeamService {
    /**
     * Sample usage:
     * <p>
     * curl -X POST $HOST:$PORT/nationalteam \
     * -H "Content-Type: application/json" --data \
     * '{"nationalteamId":123,"name":"name 123","teamSelector":"selector"}'
     *
     * @param body
     * @return
     */
    @PostMapping(
            value = "/nationalteam",
            consumes = "application/json",
            produces = "application/json")
    NationalTeam createNationalTeam(@RequestBody NationalTeam body);

    /**
     * Sample usage: curl $HOST:$PORT/nationalteam/1
     *
     * @param nationalteamId
     * @return the national team, if found, else null
     */
    @GetMapping(
            value = "/nationalteam/{nationalteamId}",
            produces = "application/json")
    NationalTeam getNationalTeam(@PathVariable int nationalteamId);

    /**
     * Sample usage:
     * <p>
     * curl -X DELETE $HOST:$PORT/nationalteam/1
     *
     * @param nationalteamId
     */
    @DeleteMapping(value = "/nationalteam/{nationalteamId}")
    void deleteNationalTeam(@PathVariable int nationalteamId);
}