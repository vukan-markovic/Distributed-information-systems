package se.magnus.api.core.nationalteam;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

public interface NationalTeamService {
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
    Mono<NationalTeam> getNationalTeam(@PathVariable int nationalteamId);

    void deleteNationalTeam(@PathVariable int nationalteamId);
}