package se.magnus.api.composite.player;

public class NationalitySummary {
    private final int nationalityId;
    private final String name;
    private final String abbreviation;

    public NationalitySummary() {
        this.nationalityId = 0;
        this.name = null;
        this.abbreviation = null;
    }

    public NationalitySummary(int nationalityId, String name, String abbreviation, String content) {
        this.nationalityId = nationalityId;
        this.name = name;
        this.abbreviation = abbreviation;
    }

    public int getNationalityId() {
        return nationalityId;
    }

    public String getName() {
        return name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}