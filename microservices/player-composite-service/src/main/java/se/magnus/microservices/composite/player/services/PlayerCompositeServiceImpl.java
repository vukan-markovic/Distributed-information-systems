package se.magnus.microservices.composite.player.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.composite.player.*;
import se.magnus.api.core.league.League;
import se.magnus.api.core.nationality.Nationality;
import se.magnus.api.core.nationalteam.NationalTeam;
import se.magnus.api.core.player.Player;
import se.magnus.api.core.team.Team;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.net.URL;
import java.util.List;

@RestController
public class PlayerCompositeServiceImpl implements PlayerCompositeService {
    private static final Logger LOG = LoggerFactory.getLogger(PlayerCompositeServiceImpl.class);
    private final SecurityContext nullSC = new SecurityContextImpl();
    private final ServiceUtil serviceUtil;
    private final PlayerCompositeIntegration integration;

    @Autowired
    public PlayerCompositeServiceImpl(ServiceUtil serviceUtil, PlayerCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public void createCompositePlayer(PlayerAggregate body) {
        internalCreateCompositePlayer(SecurityContextHolder.getContext(), body);
    }

    public void internalCreateCompositePlayer(SecurityContext context, PlayerAggregate body) {
        try {
            logAuthorizationInfo(context);
            LOG.debug("createCompositePlayer: creates a new composite entity for playerId: {}", body.getPlayerId());

            Player player = new Player(body.getPlayerId(),
                    body.getName(),
                    body.getSurname(),
                    body.getRegistrationNumber(),
                    body.getDateOfBirth(),
                    1,
                    body.getNationality().getNationalityId(),
                    body.getTeam().getTeamId(),
                    body.getLeague().getLeagueId(),
                    null);

            integration.createPlayer(player);

            if (body.getTeam() != null) {
                Team team = new Team(body.getTeam().getTeamId(), body.getTeam().getName(), body.getTeam().getFounded(), body.getTeam().getCity(), null);
                integration.createTeam(team);
            }

            if (body.getNationalTeam() != null) {
                NationalTeam nationalteam = new NationalTeam(body.getNationalTeam().getNationalTeamId(), body.getNationalTeam().getName(), body.getNationalTeam().getTeamSelector(), null);
                integration.createNationalTeam(nationalteam);
            }

            if (body.getNationality() != null) {
                Nationality nationality = new Nationality(body.getNationality().getNationalityId(), body.getNationality().getName(), body.getNationality().getAbbreviation(), null);
                integration.createNationality(nationality);
            }

            if (body.getLeague() != null) {
                League league = new League(body.getLeague().getLeagueId(), body.getLeague().getName(), body.getLeague().getLabel(), null);
                integration.createLeague(league);
            }

            LOG.debug("createCompositePlayer: composite entities created for playerId: {}", body.getPlayerId());
        } catch (RuntimeException exception) {
            LOG.warn("createCompositePlayer failed: {}", exception.getLocalizedMessage());
            throw exception;
        }
    }

    @Override
    public PlayerAggregate getCompositePlayer(int playerId) {
        LOG.debug("getCompositePlayer: lookup a player aggregate for playerId: {}", playerId);

        Player player = integration.getPlayer(playerId);
        if (player == null) throw new NotFoundException("No player found for playerId: " + playerId);

        Nationality nationality = integration.getNationality(player.getNationalityId());
        NationalTeam nationalTeam = integration.getNationalTeam(player.getNationalTeamId());
        Team team = integration.getTeam(player.getTeamId());
        League league = integration.getLeague(player.getLeagueId());

        LOG.debug("getCompositePlayer: aggregate entity found for playerId: {}", playerId);

        return createPlayerAggregate(SecurityContextHolder.getContext(), player, team, nationalTeam, nationality, league, serviceUtil.getServiceAddress());
    }

    @Override
    public void deleteCompositePlayer(int playerId) {
        internalDeleteCompositePlayer(SecurityContextHolder.getContext(), playerId);
    }

    private void internalDeleteCompositePlayer(SecurityContext context, int playerId) {
        try {
            logAuthorizationInfo(context);
            LOG.debug("deleteCompositePlayer: Deletes a player aggregate for playerId: {}", playerId);
            integration.deletePlayer(playerId);
        } catch (RuntimeException exception) {
            LOG.warn("deleteCompositePlayer failed: {}", exception.toString());
            throw exception;
        }
    }

    private Player getPlayerFallbackValue(int playerId) {
        LOG.warn("Creating a fallback player for playerId = {}", playerId);

        if (playerId == 13) {
            String errMsg = "Player Id: " + playerId + " not found in fallback cache!";
            LOG.warn(errMsg);
            throw new NotFoundException(errMsg);
        }

        return new Player(playerId, "Fallback player" + playerId, "surname", "reg num", "02.02.2021.", 1, 1, 1, 1, serviceUtil.getServiceAddress());
    }

    private PlayerAggregate createPlayerAggregate(SecurityContext context, Player player, Team team, NationalTeam nationalteam, Nationality nationality, League league, String serviceAddress) {
        logAuthorizationInfo(context);

        LeagueSummary leagueSummary = (league == null) ? null :
                new LeagueSummary(league.getLeagueId(), league.getName(), league.getLabel());

        TeamSummary teamSummary = (team == null) ? null :
                new TeamSummary(team.getTeamId(), team.getName(), team.getFounded(), team.getCity());

        NationalitySummary nationalitySummary = (nationality == null) ? null :
                new NationalitySummary(nationality.getNationalityId(), nationality.getName(), nationality.getAbbreviation());

        NationalTeamSummary nationalteamSummary = (nationalteam == null) ? null :
                new NationalTeamSummary(nationalteam.getNationalTeamId(), nationalteam.getName(), nationalteam.getTeamSelector());

        String playerAddress = player.getServiceAddress();
        String nationalityAddress = (nationality != null) ? nationality.getServiceAddress() : "";
        String teamAddress = (team != null) ? team.getServiceAddress() : "";
        String nationalteamAddress = (nationalteam != null) ? nationalteam.getServiceAddress() : "";
        String leagueAddress = (league != null) ? league.getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, playerAddress, nationalityAddress, teamAddress, leagueAddress, nationalteamAddress);
        return new PlayerAggregate(player.getPlayerId(), player.getName(), player.getSurname(), player.getRegistrationNumber(), player.getDateOfBirth(), teamSummary, nationalitySummary, nationalteamSummary, leagueSummary, serviceAddresses);
    }

    private void logAuthorizationInfo(SecurityContext context) {
        if (context != null && context.getAuthentication() != null && context.getAuthentication() instanceof JwtAuthenticationToken) {
            Jwt jwtToken = ((JwtAuthenticationToken) context.getAuthentication()).getToken();
            logAuthorizationInfo(jwtToken);
        } else LOG.warn("No JWT based Authentication supplied, running tests are we?");
    }

    private void logAuthorizationInfo(Jwt jwt) {
        if (jwt == null) LOG.warn("No JWT supplied, running tests are we?");
        else {
            if (LOG.isDebugEnabled()) {
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getClaims().get("sub");
                Object scopes = jwt.getClaims().get("scope");
                Object expires = jwt.getClaims().get("exp");
                LOG.debug("Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}", subject, scopes, expires, issuer, audience);
            }
        }
    }
}