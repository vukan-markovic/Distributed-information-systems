package se.magnus.api.core.player;

public class Player {
    private int playerId;
    private String name;
    private String surname;
    private String registrationNumber;
    private String dateOfBirth;
    private String serviceAddress;
    private int nationalityId;
    private int teamId;
    private int nationalteamId;
    private int leagueId;

    public Player() {
        playerId = 0;
        name = null;
        surname = null;
        registrationNumber = null;
        dateOfBirth = null;
        serviceAddress = null;
        nationalityId = 0;
        teamId = 0;
        nationalteamId = 0;
        leagueId = 0;
    }

    public Player(int playerId, String name, String surname, String registrationNumber, String dateOfBirth, int nationalityId, int nationalteamId, int teamId, int leagueId, String serviceAddress) {
        this.playerId = playerId;
        this.name = name;
        this.surname = surname;
        this.registrationNumber = registrationNumber;
        this.dateOfBirth = dateOfBirth;
        this.nationalteamId = nationalteamId;
        this.nationalityId = nationalityId;
        this.teamId = teamId;
        this.leagueId = leagueId;
        this.serviceAddress = serviceAddress;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getNationalityId() {
        return nationalityId;
    }

    public void setNationalityId(int nationalityId) {
        this.nationalityId = nationalityId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getNationalTeamId() {
        return nationalteamId;
    }

    public void setNationalTeamId(int nationalteamId) {
        this.nationalteamId = nationalteamId;
    }

    public int getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(int leagueId) {
        this.leagueId = leagueId;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}