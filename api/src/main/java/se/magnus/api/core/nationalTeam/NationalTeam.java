package se.magnus.api.core.nationalTeam;

public class NationalTeam {
    private int nationalTeamId;
    private String name;
    private String teamSelector;
    private String serviceAddress;

    public NationalTeam() {
        nationalTeamId = 0;
        name = null;
        teamSelector = null;
        serviceAddress = null;
    }

    public NationalTeam(int nationalTeamId, String name, String teamSelector, String serviceAddress) {
        this.nationalTeamId = nationalTeamId;
        this.name = name;
        this.teamSelector = teamSelector;
        this.serviceAddress = serviceAddress;
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

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}