package se.magnus.api.core.league;

public class League {
    private int leagueId;
    private String name;
    private String label;
    private String serviceAddress;

    public League() {
        leagueId = 0;
        name = null;
        label = null;
        serviceAddress = null;
    }

    public League(int leagueId, String name, String label, String serviceAddress) {
        this.leagueId = leagueId;
        this.name = name;
        this.label = label;
        this.serviceAddress = serviceAddress;
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

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}