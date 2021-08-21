package se.magnus.api.core.nationalteam;

public class NationalTeam {
    private int nationalteamId;
    private String name;
    private String teamSelector;
    private String serviceAddress;

    public NationalTeam() {
        nationalteamId = 0;
        name = null;
        teamSelector = null;
        serviceAddress = null;
    }

    public NationalTeam(int nationalteamId, String name, String teamSelector, String serviceAddress) {
        this.nationalteamId = nationalteamId;
        this.name = name;
        this.teamSelector = teamSelector;
        this.serviceAddress = serviceAddress;
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

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}