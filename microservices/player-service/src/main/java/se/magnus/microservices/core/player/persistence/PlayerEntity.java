package se.magnus.microservices.core.player.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import static java.lang.String.format;

@Document(collection = "players")
@CompoundIndex(name = "player-id", unique = true, def = "{'playerId': 1, 'nationalityId': 1, 'nationalteamId': 1, 'teamId': 1, 'leagueId: 1'}")
public class PlayerEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    @Indexed(unique = true)
    private int playerId;

    private String name;
    private String surname;
    private String registrationNumber;
    private String dateOfBirth;
    private int nationalteamId;
    private int nationalityId;
    private int teamId;
    private int leagueId;

    public PlayerEntity() {
    }

    public PlayerEntity(int playerId, String name, String surname, String registrationNumber, String dateOfBirth, int nationalteamId, int nationalityId, int teamId, int leagueId) {
        this.playerId = playerId;
        this.name = name;
        this.surname = surname;
        this.registrationNumber = registrationNumber;
        this.dateOfBirth = dateOfBirth;
        this.nationalteamId = nationalteamId;
        this.nationalityId = nationalityId;
        this.teamId = teamId;
        this.leagueId = leagueId;
    }

    @Override
    public String toString() {
        return format("PlayerEntity: %s", playerId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getNationalTeamId() {
        return nationalteamId;
    }

    public void setNationalTeamId(int nationalteamId) {
        this.nationalteamId = nationalteamId;
    }

    public int getNationalityId() {
        return nationalityId;
    }

    public void setNationalityId(int nationalityId) {
        this.nationalityId = nationalityId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(int leagueId) {
        this.leagueId = leagueId;
    }
}