package se.magnus.api.composite.player;

public class ServiceAddresses {
    private final String composite;
    private final String player;
    private final String nationality;
    private final String team;
    private final String league;
    private final String nationalTeam;

    public ServiceAddresses() {
        composite = null;
        player = null;
        nationality = null;
        team = null;
        league = null;
        nationalTeam = null;
    }

    public ServiceAddresses(String compositeAddress, String playerAddress, String nationalityAddress, String teamAddress,
                            String leagueAddress, String nationalTeamAddress) {
        this.composite = compositeAddress;
        this.player = playerAddress;
        this.nationality = nationalityAddress;
        this.team = teamAddress;
        this.league = leagueAddress;
        this.nationalTeam = nationalTeamAddress;
    }

    public String getComposite() {
        return composite;
    }

    public String getPlayer() {
        return player;
    }

    public String getNationality() {
        return nationality;
    }

    public String getTeam() {
        return team;
    }

    public String getLeague() {
        return league;
    }

    public String getNationalTeam() {
        return nationalTeam;
    }
}