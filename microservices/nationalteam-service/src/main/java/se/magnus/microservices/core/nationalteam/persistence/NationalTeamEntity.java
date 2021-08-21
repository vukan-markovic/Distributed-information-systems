package se.magnus.microservices.core.nationalteam.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import static java.lang.String.format;

@Document(collection = "nationalteams")
public class NationalTeamEntity {
    @Id
    private String id;

    @Version
    private Integer version;

    @Indexed(unique = true)
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