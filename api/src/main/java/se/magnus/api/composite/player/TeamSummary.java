package se.magnus.api.composite.player;

public class TeamSummary {
    private final int teamId;
    private final String name;
    private final String founded;
    private final String city;
    private final LeagueSummary league;

    public TeamSummary() {
        this.teamId = 0;
        this.name = null;
        this.founded = null;
        this.city = null;
        this.league = null;
    }

    public TeamSummary(int teamId, String name, String founded, String city, LeagueSummary league) {
        this.teamId = teamId;
        this.name = name;
        this.founded = founded;
        this.city = city;
        this.league = league;
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

    public LeagueSummary getLeague() {
        return league;
    }
}