package se.magnus.api.core.team;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

public interface TeamService {
    Team createTeam(@RequestBody Team body);

    /**
     * Sample usage:
     * <p>
     * curl $HOST:$PORT/team/1
     *
     * @param teamId
     * @return
     */
    @GetMapping(
            value = "/team",
            produces = "application/json")
    Mono<Team> getTeam(@RequestParam(value = "teamId", required = true) int teamId);

    void deleteTeam(@RequestParam(value = "teamId", required = true) int teamId);
}