package se.magnus.microservices.composite.player.services;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import se.magnus.api.composite.player.*;
import se.magnus.api.core.nationalTeam.NationalTeam;
import se.magnus.api.core.nationality.Nationality;
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
    public Mono<Void> createCompositePlayer(PlayerAggregate body) {
        return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalCreateCompositePlayer(sc, body)).then();
    }

    public void internalCreateCompositePlayer(SecurityContext sc, PlayerAggregate body) {
        try {
            logAuthorizationInfo(sc);
            LOG.debug("createCompositePlayer: creates a new composite entity for playerId: {}", body.getPlayerId());
            Player player = new Player(body.getPlayerId(), body.getName(), body.getSurname(), body.getRegistration_number(), body.getDateOfBirth(), null);
            integration.createPlayer(player);

            if (body.getTeam() != null) {
                Team team = new Team(body.getPlayerId(), body.getTeam().getName(), r.getAuthor(), r.getRate(), r.getContent(), null);
                integration.createTeam(team);
            }

            if (body.getNationality() != null) {
                NationalTeam nationalTeam = new NationalTeam(body.getPlayerId(),   body.getNationality().getNationalityId(),   body.getNationality().getName(),   body.getNationality().getAbbreviation(), r.getContent(), null);
                integration.createNationalTeam(nationalTeam);
              }

            LOG.debug("createCompositePlayer: composite entities created for playerId: {}", body.getPlayerId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositePlayer failed: {}", re.toString());
            throw re;
        }
    }

    @Override
    public Mono<PlayerAggregate> getCompositePlayer(int playerId, int delay, int faultPercent) {
        return Mono.zip(
                values -> createPlayerAggregate((SecurityContext) values[0], (Player) values[1], (Team) values[2], (NationalTeam) values[3], serviceUtil.getServiceAddress()),
                ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSC),
                integration.getPlayer(playerId, delay, faultPercent)
                        .onErrorReturn(CallNotPermittedException.class, getPlayerFallbackValue(playerId)),
                integration.getNationalTeam(playerId),
                integration.getTeam(playerId))
                .doOnError(ex -> LOG.warn("getCompositePlayer failed: {}", ex.toString()))
                .log();
    }

    @Override
    public Mono<Void> deleteCompositePlayer(int playerId) {
        return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalDeleteCompositePlayer(sc, playerId)).then();
    }

    private void internalDeleteCompositePlayer(SecurityContext sc, int playerId) {
        try {
            logAuthorizationInfo(sc);
            LOG.debug("deleteCompositePlayer: Deletes a player aggregate for playerId: {}", playerId);
            integration.deletePlayer(playerId);
            integration.deleteTeam(playerId);
            integration.deleteNationalTeam(playerId);
            LOG.debug("deleteCompositePlayer: aggregate entities deleted for playerId: {}", playerId);
        } catch (RuntimeException re) {
            LOG.warn("deleteCompositePlayer failed: {}", re.toString());
            throw re;
        }
    }

    private Player getPlayerFallbackValue(int playerId) {
        LOG.warn("Creating a fallback player for playerId = {}", playerId);

        if (playerId == 13) {
            String errMsg = "Player Id: " + playerId + " not found in fallback cache!";
            LOG.warn(errMsg);
            throw new NotFoundException(errMsg);
        }

        return new Player(playerId, "Fallback player" + playerId, playerId, serviceUtil.getServiceAddress());
    }

    private PlayerAggregate createPlayerAggregate(SecurityContext sc, Player player, Team team, NationalTeam nationalTeam, Nationality nationality, String serviceAddress) {
        logAuthorizationInfo(sc);
        int playerId = player.getPlayerId();
        String name = player.getName();
        String surname = player.getSurname();

        TeamSummary teamSummary = (team == null) ? null :
                 new TeamSummary(r.getLeagueId(), r.getName(), r.getFounded(), r.getCity());

        NationalitySummary nationalitySummary = (nationality == null) ? null :
               new NationalitySummary(r.getNationalityId(), r.getName(), r.getAbbreviation(), r.getContent());

        String playerAddress = player.getServiceAddress();
        String nationalityAddress = (nationality != null) ? nationality.getServiceAddress() : "";
        String teamAdress = (team != null) ? team.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, playerAddress, reviewAddress, recommendationAddress);

        return new PlayerAggregate(playerId, name, surname, teamAdress, nationalityAddress, serviceAddresses);
    }

    private void logAuthorizationInfo(SecurityContext sc) {
        if (sc != null && sc.getAuthentication() != null && sc.getAuthentication() instanceof JwtAuthenticationToken) {
            Jwt jwtToken = ((JwtAuthenticationToken) sc.getAuthentication()).getToken();
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