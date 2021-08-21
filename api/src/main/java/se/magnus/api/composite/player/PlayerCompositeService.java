package se.magnus.api.composite.player;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Api(description = "REST API for composite player information.")
public interface PlayerCompositeService {
    /**
     * Sample usage:
     * <p>
     * curl -X POST $HOST:$PORT/player-composite \
     * -H "Content-Type: application/json" --data \
     * '{"playerId":123,"name":"Cristiano","surname":"Ronaldo","registrationNumber":"324343","dateOfBirth":"1980-01-21", "nationalityId": 1, "teamId": 1,"nationalteamId":1}'
     *
     * @param body
     */
    @ApiOperation(
            value = "${api.player-composite.create-composite-player.description}",
            notes = "${api.player-composite.create-composite-player.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @PostMapping(
            value = "/player-composite",
            consumes = "application/json")
    Mono<Void> createCompositePlayer(@RequestBody PlayerAggregate body);

    /**
     * Sample usage: curl $HOST:$PORT/player-composite/1
     *
     * @param playerId
     * @return the composite player info, if found, else null
     */
    @ApiOperation(
            value = "${api.player-composite.get-composite-player.description}",
            notes = "${api.player-composite.get-composite-player.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 404, message = "Not found, the specified id does not exist."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @GetMapping(
            value = "/player-composite/{playerId}",
            produces = "application/json")
    Mono<PlayerAggregate> getCompositePlayer(
            @PathVariable int playerId,
            @RequestParam(value = "delay", required = false, defaultValue = "0") int delay,
            @RequestParam(value = "faultPercent", required = false, defaultValue = "0") int faultPercent
    );

    /**
     * Sample usage:
     * <p>
     * curl -X DELETE $HOST:$PORT/player-composite/1
     *
     * @param playerId
     */
    @ApiOperation(
            value = "${api.player-composite.delete-composite-player.description}",
            notes = "${api.player-composite.delete-composite-player.notes}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @DeleteMapping(value = "/player-composite/{playerId}")
    Mono<Void> deleteCompositePlayer(@PathVariable int playerId);
}