package se.magnus.api.core.nationalTeam;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

public interface NationalTeamService {
    NationalTeam createNationalTeam(@RequestBody NationalTeam body);

    /**
     * Sample usage: curl $HOST:$PORT/nationalTeam?nationalTeamId=1
     *
     * @param nationalTeamId
     * @return the national team, if found, else null
     */
    @GetMapping(
            value = "/nationalTeam/{nationalTeamId}",
            produces = "application/json")
    Mono<NationalTeam> getNationalTeam(@PathVariable int nationalTeamId);

    void deleteNationalTeam(@PathVariable int nationalTeamId);
}