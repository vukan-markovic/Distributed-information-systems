package se.magnus.microservices.composite.player.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import se.magnus.api.core.league.League;
import se.magnus.api.core.league.LeagueService;
import se.magnus.api.core.nationality.Nationality;
import se.magnus.api.core.nationality.NationalityService;
import se.magnus.api.core.nationalteam.NationalTeam;
import se.magnus.api.core.nationalteam.NationalTeamService;
import se.magnus.api.core.player.Player;
import se.magnus.api.core.player.PlayerService;
import se.magnus.api.core.team.Team;
import se.magnus.api.core.team.TeamService;
import se.magnus.util.exceptions.InvalidInputException;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

import java.io.IOException;
import java.net.URI;

@Component
public class PlayerCompositeIntegration implements PlayerService, TeamService, NationalTeamService, NationalityService, LeagueService {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerCompositeIntegration.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String playerServiceUrl = "http://player";
    private final String nationalteamServiceUrl = "http://nationalteam";
    private final String teamServiceUrl = "http://team";
    private final String nationalityServiceUrl = "http://nationality";
    private final String leagueServiceUrl = "http://league";

    @Autowired
    public PlayerCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper mapper
    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    @Override
    public Player createPlayer(Player body) {
        try {
            String url = playerServiceUrl;
            LOG.debug("Will post a new player to URL: {}", url);

            Player player = restTemplate.postForObject(url, body, Player.class);
            LOG.debug("Created a player with id: {}", player.getPlayerId());

            return player;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Player getPlayer(int playerId) {
        try {
            URI url = UriComponentsBuilder.fromUriString(playerServiceUrl + "/player/{playerId}").build(playerId);
            LOG.debug("Will call the getPlayer API on URL: {}", url);

            Player player = restTemplate.getForObject(url, Player.class);
            LOG.debug("Found a player with id: {}", player.getPlayerId());

            return player;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deletePlayer(int playerId) {
        try {
            URI url = UriComponentsBuilder.fromUriString(playerServiceUrl + "/player/{playerId}").build(playerId);
            LOG.debug("Will call the deletePlayer API on URL: {}", url);
            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Team createTeam(Team body) {
        try {
            String url = teamServiceUrl;
            LOG.debug("Will post a new team to URL: {}", url);

            Team team = restTemplate.postForObject(url, body, Team.class);
            LOG.debug("Created a team with id: {}", team.getTeamId());

            return team;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Team getTeam(int teamId) {
        try {
            String url = teamServiceUrl + "/" + teamId;

            LOG.debug("Will call the getTeam API on URL: {}", url);
            Team team = restTemplate.getForObject(url, Team.class);

            LOG.debug("Found team with id: {}", teamId);
            return team;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteTeam(int teamId) {
        try {
            URI url = UriComponentsBuilder.fromUriString(teamServiceUrl + "/team/{teamId}").build(teamId);
            LOG.debug("Will call the deleteTeam API on URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public NationalTeam createNationalTeam(NationalTeam body) {
        try {
            String url = nationalteamServiceUrl;
            LOG.debug("Will post a new national team to URL: {}", url);

            NationalTeam nationalTeam = restTemplate.postForObject(url, body, NationalTeam.class);
            LOG.debug("Created a national team with id: {}", nationalTeam.getNationalTeamId());

            return nationalTeam;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public NationalTeam getNationalTeam(int nationalteamId) {
        try {
            URI url = UriComponentsBuilder.fromUriString(nationalteamServiceUrl + "/nationalteam/{nationalteamId}").build(nationalteamId);
            LOG.debug("Will call the getNationalTeam API on URL: {}", url);
            NationalTeam nationalTeam = restTemplate.getForObject(url, NationalTeam.class);

            LOG.debug("Found national team with id: {}", nationalteamId);
            return nationalTeam;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteNationalTeam(int nationalteamId) {
        try {
            URI url = UriComponentsBuilder.fromUriString(nationalteamServiceUrl + "/nationalteam/{nationalteamId}").build(nationalteamId);
            LOG.debug("Will call the deleteNationalTeam API on URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Nationality createNationality(Nationality body) {
        try {
            String url = nationalityServiceUrl;
            LOG.debug("Will post a new nationality to URL: {}", url);

            Nationality nationality = restTemplate.postForObject(url, body, Nationality.class);
            LOG.debug("Created a nationality with id: {}", nationality.getNationalityId());

            return nationality;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Nationality getNationality(int nationalityId) {
        try {
            URI url = UriComponentsBuilder.fromUriString(nationalityServiceUrl + "/nationality/{nationalityId}").build(nationalityId);
            LOG.debug("Will call the getNationality API on URL: {}", url);
            Nationality nationality = restTemplate.getForObject(url, Nationality.class);

            LOG.debug("Found nationality with id: {}", nationalityId);
            return nationality;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteNationality(int nationalityId) {
        try {
            URI url = UriComponentsBuilder.fromUriString(nationalityServiceUrl + "/nationality/{nationalityId}").build(nationalityId);
            LOG.debug("Will call the deleteNationality API on URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public League createLeague(League body) {
        try {
            String url = leagueServiceUrl;
            LOG.debug("Will post a new league to URL: {}", url);

            League league = restTemplate.postForObject(url, body, League.class);
            LOG.debug("Created a league with id: {}", league.getLeagueId());

            return league;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public League getLeague(int leagueId) {
        try {
            URI url = UriComponentsBuilder.fromUriString(leagueServiceUrl + "/league/{leagueId}").build(leagueId);

            LOG.debug("Will call the getLeague API on URL: {}", url);
            League league = restTemplate.getForObject(url, League.class);

            LOG.debug("Found league with id: {}", leagueId);
            return league;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteLeague(int leagueId) {
        try {
            URI url = UriComponentsBuilder.fromUriString(leagueServiceUrl + "/league/{leagueId}").build(leagueId);
            LOG.debug("Will call the deleteLeague API on URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (ex.getStatusCode()) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(ex));

            case UNPROCESSABLE_ENTITY:
                return new InvalidInputException(getErrorMessage(ex));

            default:
                LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}