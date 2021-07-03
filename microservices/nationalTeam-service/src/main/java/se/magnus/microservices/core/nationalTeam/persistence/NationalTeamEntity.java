package se.magnus.microservices.core.nationalTeam.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import static java.lang.String.format;

@Document(collection="nationalTeams")
@CompoundIndex(name = "nationalTeam-id", unique = true, def = "{'nationalTeamId': 1}")
public class NationalTeamEntity {
    @Id
    private String id;

    @Version
    private int version;

    private int nationalTeamId;
    private String name;
    private String teamSelector;

    public NationalTeamEntity() {
    }

    public NationalTeamEntity(int nationalTeamId, String name, String teamSelector) {
        this.nationalTeamId = nationalTeamId;
        this.name = name;
        this.teamSelector = teamSelector;
    }

    @Override
    public String toString() {
        return format("NationalTeamEntity: %d", nationalTeamId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getNationalTeamId() {
        return nationalTeamId;
    }

    public void setNationalTeamId(int nationalTeamId) {
        this.nationalTeamId = nationalTeamId;
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