package se.magnus.api.core.team;

public class Team {
    private int teamId;
    private String name;
    private String founded;
    private String city;
    private String serviceAddress;

    public Team() {
        teamId = 0;
        name = null;
        founded = null;
        city = null;
        serviceAddress = null;
    }

    public Team(int teamId, String name, String founded, String city, String serviceAddress) {
        this.teamId = teamId;
        this.name = name;
        this.founded = founded;
        this.city = city;
        this.serviceAddress = serviceAddress;
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

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}