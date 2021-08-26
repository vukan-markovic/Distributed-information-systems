package se.magnus.microservices.core.nationality.persistence;

import javax.persistence.*;

import static java.lang.String.format;

@Entity
@Table(name = "nationality", indexes = { @Index(name = "nationality_unique_idx", unique = true, columnList = "nationalityId") })
public class NationalityEntity {
    @Id
    @GeneratedValue
    private int id;

    @Version
    private int version;

    private int nationalityId;
    private String name;
    private String abbreviation;

    public NationalityEntity() {
    }

    public NationalityEntity(int nationalityId, int reviewId, String name, String abbreviation) {
        this.nationalityId = nationalityId;
        this.name = name;
        this.abbreviation = abbreviation;
    }

    @Override
    public String toString() {
        return format("NationalityEntity: %d", nationalityId);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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
}