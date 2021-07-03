package se.magnus.api.composite.player;

public class NationalTeamSummary {
    private final int nationalTeamId;
    private final String name;
    private final String teamSelector;

    public NationalTeamSummary() {
        this.nationalTeamId = 0;
        this.name = null;
        this.teamSelector = null;
    }

    public NationalTeamSummary(int nationalTeamId, String name, String teamSelector, String content) {
        this.nationalTeamId = nationalTeamId;
        this.name = name;
        this.teamSelector = teamSelector;
    }

    public int getNationalTeamId() {
        return nationalTeamId;
    }

    public String getName() {
        return name;
    }

    public String getTeamSelector() {
        return teamSelector;
    }
}