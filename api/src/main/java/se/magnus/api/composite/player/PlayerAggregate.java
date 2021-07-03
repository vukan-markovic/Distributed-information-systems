package se.magnus.api.composite.player;

public class PlayerAggregate {
    private final int playerId;
    private final String name;
    private final String surname;
    private final String registration_number;
    private final String dateOfBirth;
    private final TeamSummary team;
    private final NationalitySummary nationality;
    private final NationalTeamSummary nationalTeam;
    private final ServiceAddresses serviceAddresses;

    public PlayerAggregate() {
        playerId = 0;
        name = null;
        surname = null;
        registration_number = null;
        dateOfBirth = null;
        team = null;
        nationality = null;
        nationalTeam = null;
        serviceAddresses = null;
    }

    public PlayerAggregate(
            int playerId,
            String name,
            String surname,
            String registration_number,
            String dateOfBirth,
            TeamSummary team,
            NationalitySummary nationality,
            NationalTeamSummary nationalTeam,
            ServiceAddresses serviceAddresses) {
        this.playerId = playerId;
        this.name = name;
        this.surname = surname;
        this.registration_number = registration_number;
        this.dateOfBirth = dateOfBirth;
        this.team = team;
        this.nationality = nationality;
        this.nationalTeam = nationalTeam;
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

    public String getRegistration_number() {
        return registration_number;
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
        return nationalTeam;
    }

    public ServiceAddresses getServiceAddresses() {
        return serviceAddresses;
    }
}