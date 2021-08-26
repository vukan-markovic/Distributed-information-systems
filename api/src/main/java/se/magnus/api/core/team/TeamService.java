package se.magnus.api.core.team;

import org.springframework.web.bind.annotation.*;

public interface TeamService {
    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/team \
     *   -H "Content-Type: application/json" --data \
     *   '{"teamId":123,"name":"name 123","founded":"01.02.2015.","city": "Belgrade"}'
     *
     * @param body
     * @return
     */
    @PostMapping(
            value    = "/team",
            consumes = "application/json",
            produces = "application/json")
    Team createTeam(@RequestBody Team body);

    /**
     * Sample usage:
     * <p>
     * curl $HOST:$PORT/team/1
     *
     * @param teamId
     * @return the team, if found, else null
     */
    @GetMapping(
            value = "/team/{teamId}",
            produces = "application/json")
    Team getTeam(@PathVariable int teamId);

    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/team/1
     *
     * @param teamId
     */
    @DeleteMapping(value = "/team/{teamId}")
    void deleteTeam(@PathVariable int teamId);
}