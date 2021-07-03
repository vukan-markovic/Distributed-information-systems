package se.magnus.api.core.nationality;

public class Nationality {
    private int nationalityId;
    private String name;
    private String abbreviation;
    private String serviceAddress;

    public Nationality() {
        nationalityId = 0;
        name = null;
        abbreviation = null;
        serviceAddress = null;
    }

    public Nationality(int nationalityId, String name, String abbreviation, String serviceAddress) {
        this.nationalityId = nationalityId;
        this.name = name;
        this.abbreviation = abbreviation;
        this.serviceAddress = serviceAddress;
    }

    public int getNationalityId() {
        return nationalityId;
    }

    public void setNationalityId(int nationalityId) {
        this.nationalityId = nationalityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}