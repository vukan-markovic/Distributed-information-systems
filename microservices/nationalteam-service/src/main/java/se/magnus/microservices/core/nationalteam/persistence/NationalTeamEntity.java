package se.magnus.microservices.core.nationalteam.persistence;

import javax.persistence.*;

import static java.lang.String.format;

@Entity
@Table(name = "nationalteam", indexes = {@Index(name = "nationalteam_unique_idx", unique = true, columnList = "nationalteamId")})
public class NationalTeamEntity {
    @Id
    @GeneratedValue
    private int id;

    @Version
    private int version;

    private int nationalteamId;
    private String name;
    private String teamSelector;

    public NationalTeamEntity() {
    }

    public NationalTeamEntity(int nationalteamId, String name, String teamSelector) {
        this.nationalteamId = nationalteamId;
        this.name = name;
        this.teamSelector = teamSelector;
    }

    @Override
    public String toString() {
        return format("NationalTeamEntity: %d", nationalteamId);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getNationalTeamId() {
        return nationalteamId;
    }

    public void setNationalTeamId(int nationalteamId) {
        this.nationalteamId = nationalteamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeamSelector() {
        return teamSelector;
    }

    public void setTeamSelector(String teamSelector) {
        this.teamSelector = teamSelector;
    }
}