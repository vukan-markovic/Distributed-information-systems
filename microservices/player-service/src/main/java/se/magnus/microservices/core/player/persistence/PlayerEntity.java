package se.magnus.microservices.core.player.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import static java.lang.String.format;

@Document(collection = "players")
public class PlayerEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    @Indexed(unique = true)
    private int playerId;

    private String name;
    private String surname;
    private String registrationNumber;
    private String dateOfBirth;

    public PlayerEntity() {
    }

    public PlayerEntity(int playerId, String name, String surname, String registrationNumber, String dateOfBirth) {
        this.playerId = playerId;
        this.name = name;
        this.surname = surname;
        this.registrationNumber = registrationNumber;
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public String toString() {
        return format("PlayerEntity: %s", playerId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
}