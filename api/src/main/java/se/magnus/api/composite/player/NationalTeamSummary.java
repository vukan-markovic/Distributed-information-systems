package se.magnus.api.composite.player;

public class NationalTeamSummary {
    private final int nationalteamId;
    private final String name;
    private final String teamSelector;

    public NationalTeamSummary() {
        this.nationalteamId = 0;
        this.name = null;
        this.teamSelector = null;
    }

    public NationalTeamSummary(int nationalteamId, String name, String teamSelector) {
        this.nationalteamId = nationalteamId;
        this.name = name;
        this.teamSelector = teamSelector;
    }

    public int getNationalTeamId() {
        return nationalteamId;
    }

    public String getName() {
        return name;
    }

    public String getTeamSelector() {
        return teamSelector;
    }
}