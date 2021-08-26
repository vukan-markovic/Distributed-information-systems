package se.magnus.api.core.nationality;

import org.springframework.web.bind.annotation.*;

public interface NationalityService {
    /**
     * Sample usage:
     * <p>
     * curl -X POST $HOST:$PORT/nationality \
     * -H "Content-Type: application/json" --data \
     * '{"nationalityId":123,"name":"nationality 123","abbreviation":"n"}'
     *
     * @param body
     * @return
     */
    @PostMapping(
            value = "/nationality",
            consumes = "application/json",
            produces = "application/json")
    Nationality createNationality(@RequestBody Nationality body);

    /**
     * Sample usage: curl $HOST:$PORT/nationality/1
     *
     * @param nationalityId
     * @return the nationality, if found, else null
     */
    @GetMapping(
            value = "/nationality/{nationalityId}",
            produces = "application/json")
    Nationality getNationality(@PathVariable int nationalityId);

    /**
     * Sample usage:
     * <p>
     * curl -X DELETE $HOST:$PORT/nationality/1
     *
     * @param nationalityId
     */
    @DeleteMapping(value = "/nationality/{nationalityId}")
    void deleteNationality(@PathVariable int nationalityId);
}