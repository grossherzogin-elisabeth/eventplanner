package org.eventplanner.users.adapter.jpa;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.eventplanner.users.entities.EncryptedUserDetails;
import org.eventplanner.users.entities.EncryptedUserQualification;
import org.eventplanner.users.values.EncryptedAddress;
import org.eventplanner.users.values.EncryptedString;
import org.eventplanner.users.values.UserKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedUserDetailsJpaEntity implements Serializable {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @Column(name = "key", nullable = false, updatable = false)
    private String key;

    @Column(name = "auth_key")
    private String authKey;

    @Column(name = "gender")
    private String gender;

    @Column(name = "title")
    private String title;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "nick_name")
    private String nickName;

    @Column(name = "second_name")
    private String secondName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "positions", nullable = false)
    private String positionsRaw;

    @Column(name = "roles", nullable = false)
    private String rolesRaw;

    @Column(name = "qualifications", nullable = false)
    private String qualificationsRaw;

    @Column(name = "address")
    private String addressRaw;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "mobile")
    private String mobile;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "place_of_birth")
    private String placeOfBirth;

    @Column(name = "pass_nr")
    private String passNr;

    @Column(name = "comment")
    private String comment;

    @Column(name = "nationality")
    private String nationality;

    public static EncryptedUserDetailsJpaEntity fromDomain(EncryptedUserDetails domain) {
        return new EncryptedUserDetailsJpaEntity(
            domain.getKey().value(),
            domain.getAuthKey() != null ? domain.getAuthKey().value() : null,
            domain.getGender() != null ? domain.getGender().value() : null,
            domain.getTitle() != null ? domain.getTitle().value() : null,
            domain.getFirstName().value(),
            domain.getNickName() != null ? domain.getNickName().value() : null,
            domain.getSecondName() != null ? domain.getSecondName().value() : null,
            domain.getLastName().value(),
            serializeEncryptedStringList(domain.getPositions()),
            serializeEncryptedStringList(domain.getRoles()),
            serializeQualifications(domain.getQualifications()),
            domain.getAddress() != null ? serializeAddress(domain.getAddress()) : null,
            domain.getEmail() != null ? domain.getEmail().value() : null,
            domain.getPhone() != null ? domain.getPhone().value() : null,
            domain.getMobile() != null ? domain.getMobile().value() : null,
            domain.getDateOfBirth() != null ? domain.getDateOfBirth().value() : null,
            domain.getPlaceOfBirth() != null ? domain.getPlaceOfBirth().value() : null,
            domain.getPassNr() != null ? domain.getPassNr().value() : null,
            domain.getComment() != null ? domain.getComment().value() : null,
            domain.getNationality() != null ? domain.getNationality().value() : null
        );
    }

    private static String serializeEncryptedStringList(List<EncryptedString> list) {
        try {
            return objectMapper.writeValueAsString(list.stream().map(EncryptedString::value).toList());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing list", e);
        }
    }

    private static List<EncryptedString> deserializeEncryptedStringList(String json) {
        try {
            List<String> list = objectMapper.readValue(json, new TypeReference<>() {});
            return list.stream().map(EncryptedString::new).toList();
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing list", e);
        }
    }

    private static String serializeQualifications(List<EncryptedUserQualification> qualifications) {
        try {
            var entities = qualifications.stream().map(EncryptedUserQualificationsJsonEntity::fromDomain).toList();
            return objectMapper.writeValueAsString(entities);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing qualifications", e);
        }
    }

    private static List<EncryptedUserQualification> deserializeQualifications(String json) {
        try {
            var entities = objectMapper.readValue(json, new TypeReference<List<EncryptedUserQualificationsJsonEntity>>() {});
            return entities.stream().map(EncryptedUserQualificationsJsonEntity::toDomain).toList();
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing qualifications", e);
        }
    }

    private static String serializeAddress(EncryptedAddress address) {
        try {
            var entity = EncryptedAddressJsonEntity.fromDomain(address);
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing address", e);
        }
    }

    private static EncryptedAddress deserializeAddress(String json) {
        try {
            var entity = objectMapper.readValue(json, EncryptedAddressJsonEntity.class);
            return entity.toDomain();
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing address", e);
        }
    }

    public EncryptedUserDetails toDomain() {
        return new EncryptedUserDetails(
            new UserKey(key),
            authKey != null ? new EncryptedString(authKey) : null,
            gender != null ? new EncryptedString(gender) : null,
            title != null ? new EncryptedString(title) : null,
            new EncryptedString(firstName),
            nickName != null ? new EncryptedString(nickName) : null,
            secondName != null ? new EncryptedString(secondName) : null,
            new EncryptedString(lastName),
            deserializeEncryptedStringList(positionsRaw),
            deserializeEncryptedStringList(rolesRaw),
            deserializeQualifications(qualificationsRaw),
            addressRaw != null ? deserializeAddress(addressRaw) : null,
            email != null ? new EncryptedString(email) : null,
            phone != null ? new EncryptedString(phone) : null,
            mobile != null ? new EncryptedString(mobile) : null,
            dateOfBirth != null ? new EncryptedString(dateOfBirth) : null,
            placeOfBirth != null ? new EncryptedString(placeOfBirth) : null,
            passNr != null ? new EncryptedString(passNr) : null,
            comment != null ? new EncryptedString(comment) : null,
            nationality != null ? new EncryptedString(nationality) : null
        );
    }
}