package se.magnus.microservices.core.team.persistence;

import org.springframework.data.annotation.Version;
import javax.persistence.*;
import static java.lang.String.format;

@Entity
@Table(name = "teams", indexes = {@Index(name = "teams_unique_idx", unique = true, columnList = "teamId,leagueId")})
public class TeamEntity {
    @Id
    @GeneratedValue
    private String id;

    @Version
    private int version;

    private int teamId;
    private int leagueId;
    private String name;
    private String founded;
    private String city;

    public TeamEntity() {
    }

    public TeamEntity(int teamId, int leagueId, String name, String founded, String city) {
        this.teamId = teamId;
        this.leagueId = leagueId;
        this.name = name;
        this.founded = founded;
        this.city = city;
    }

    @Override
    public String toString() {
        return format("TeamEntity: %s/%d", teamId, leagueId);
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