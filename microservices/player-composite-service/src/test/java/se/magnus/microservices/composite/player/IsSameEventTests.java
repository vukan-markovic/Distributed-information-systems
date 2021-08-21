package se.magnus.microservices.composite.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import se.magnus.api.core.player.Player;
import se.magnus.api.event.Event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;
import static se.magnus.microservices.composite.player.IsSameEvent.sameEventExceptCreatedAt;

@SuppressWarnings("ALL")
public class IsSameEventTests {
    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testEventObjectCompare() throws JsonProcessingException {
        Event<Integer, Player> event1 = new Event<>(CREATE, 1, new Player(1, "name", "surname", "reg number", "02.02.2021", 1, 1, 1, 1, null));
        Event<Integer, Player> event2 = new Event<>(CREATE, 1, new Player(1, "name", "surname", "reg number", "02.02.2021", 1, 1, 1, 1, null));
        Event<Integer, Player> event3 = new Event<>(DELETE, 1, null);
        Event<Integer, Player> event4 = new Event<>(CREATE, 1, new Player(2, "name", "surname", "reg number", "02.02.2021", 1, 1, 1, 1, null));
        String event1JSon = mapper.writeValueAsString(event1);
        assertThat(event1JSon, is(sameEventExceptCreatedAt(event2)));
        assertThat(event1JSon, not(sameEventExceptCreatedAt(event3)));
        assertThat(event1JSon, not(sameEventExceptCreatedAt(event4)));
    }
}