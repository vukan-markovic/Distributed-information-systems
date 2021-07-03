package se.magnus.api.core.nationality;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

public interface NationalityService {
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
    Mono<Nationality> getNationality(@PathVariable int nationalityId);

    void deleteNationality(@PathVariable int nationalityId);
}