package se.magnus.api.composite.player;

public class LeagueSummary {
    private final int leagueId;
    private final String name;
    private final String label;

    public LeagueSummary() {
        this.leagueId = 0;
        this.name = null;
        this.label = null;
    }

    public LeagueSummary(int leagueId, String name, String label) {
        this.leagueId = leagueId;
        this.name = name;
        this.label = label;
    }

    public int getLeagueId() {
        return leagueId;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }
}