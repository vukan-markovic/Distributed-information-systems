package se.magnus.microservices.core.league.persistence;

import javax.persistence.*;

import static java.lang.String.format;

@Entity
@Table(name = "league", indexes = {@Index(name = "league_unique_idx", unique = true, columnList = "leagueId")})
public class LeagueEntity {
    @Id
    @GeneratedValue
    private int id;

    @Version
    private int version;

    private int leagueId;
    private String name;
    private String label;

    public LeagueEntity() {
    }

    public LeagueEntity(int leagueId, String name, String label) {
        this.leagueId = leagueId;
        this.name = name;
        this.label = label;
    }

    @Override
    public String toString() {
        return format("LeagueEntity: %d", leagueId);
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

    public int getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(int leagueId) {
        this.leagueId = leagueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}