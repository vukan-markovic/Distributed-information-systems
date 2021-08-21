package se.magnus.api.composite.player;

public class PlayerAggregate {
    private final int playerId;
    private final String name;
    private final String surname;
    private final String registrationNumber;
    private final String dateOfBirth;
    private final TeamSummary team;
    private final NationalitySummary nationality;
    private final NationalTeamSummary nationalteam;
    private final LeagueSummary league;
    private final ServiceAddresses serviceAddresses;

    public PlayerAggregate() {
        playerId = 0;
        name = null;
        surname = null;
        registrationNumber = null;
        dateOfBirth = null;
        team = null;
        nationality = null;
        nationalteam = null;
        league = null;
        serviceAddresses = null;
    }

    public PlayerAggregate(
            int playerId,
            String name,
            String surname,
            String registrationNumber,
            String dateOfBirth,
            TeamSummary team,
            NationalitySummary nationality,
            NationalTeamSummary nationalteam,
            LeagueSummary league,
            ServiceAddresses serviceAddresses) {
        this.playerId = playerId;
        this.name = name;
        this.surname = surname;
        this.registrationNumber = registrationNumber;
        this.dateOfBirth = dateOfBirth;
        this.team = team;
        this.nationality = nationality;
        this.nationalteam = nationalteam;
        this.league = league;
        this.serviceAddresses = serviceAddresses;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public TeamSummary getTeam() {
        return team;
    }

    public NationalitySummary getNationality() {
        return nationality;
    }

    public NationalTeamSummary getNationalTeam() {
        return nationalteam;
    }

    public LeagueSummary getLeague() {
        return league;
    }

    public ServiceAddresses getServiceAddresses() {
        return serviceAddresses;
    }
}