package se.magnus.api.composite.player;

public class TeamSummary {
    private final int teamId;
    private final String name;
    private final String founded;
    private final String city;

    public TeamSummary() {
        this.teamId = 0;
        this.name = null;
        this.founded = null;
        this.city = null;
    }

    public TeamSummary(int teamId, String name, String founded, String city) {
        this.teamId = teamId;
        this.name = name;
        this.founded = founded;
        this.city = city;
    }

    public int getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
    }

    public String getFounded() {
        return founded;
    }

    public String getCity() {
        return city;
    }
}