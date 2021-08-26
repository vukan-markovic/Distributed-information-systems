package se.magnus.microservices.core.team.persistence;

import javax.persistence.*;

import static java.lang.String.format;

@Entity
@Table(name = "team", indexes = { @Index(name = "team_unique_idx", unique = true, columnList = "teamId") })
public class TeamEntity {
    @Id
    @GeneratedValue
    private int id;

    @Version
    private int version;

    private int teamId;
    private String name;
    private String founded;
    private String city;

    public TeamEntity() {
    }

    public TeamEntity(int teamId, String name, String founded, String city) {
        this.teamId = teamId;
        this.name = name;
        this.founded = founded;
        this.city = city;
    }

    @Override
    public String toString() {
        return format("TeamEntity: %s", teamId);
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

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFounded() {
        return founded;
    }

    public void setFounded(String founded) {
        this.founded = founded;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}