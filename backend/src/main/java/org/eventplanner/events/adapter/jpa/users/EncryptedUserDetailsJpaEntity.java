package org.eventplanner.events.adapter.jpa.users;

import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;

import org.eventplanner.common.Encrypted;
import org.eventplanner.config.ObjectMapperFactory;
import org.eventplanner.events.domain.entities.users.EncryptedEmergencyContact;
import org.eventplanner.events.domain.entities.users.EncryptedUserDetails;
import org.eventplanner.events.domain.entities.users.EncryptedUserQualification;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.events.domain.values.users.EncryptedAddress;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.users.UserKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedUserDetailsJpaEntity implements Serializable {

    private static final ObjectMapper objectMapper = ObjectMapperFactory.defaultObjectMapper();

    @Id
    @Column(name = "key", nullable = false, updatable = false)
    private String key;

    @Column(name = "oidc_id")
    private String authKey;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    @Column(name = "verified_at")
    private String verifiedAt;

    @Column(name = "last_login_at")
    private String lastLoginAt;

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

    @Column(name = "phone_work")
    private String phoneWork;

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

    @Column(name = "emergency_contact")
    private String emergencyContactRaw;

    @Column(name = "diseases")
    private String diseases;

    @Column(name = "intolerances")
    private String intolerances;

    @Column(name = "medication")
    private String medication;

    @Column(name = "diet")
    private String diet;

    public static EncryptedUserDetailsJpaEntity fromDomain(EncryptedUserDetails domain) {
        String roles = "[]";
        try {
            roles = objectMapper.writeValueAsString(domain.getRoles());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize roles for user {}", domain.getKey(), e);
        }

        String qualifications = "[]";
        try {
            qualifications = objectMapper.writeValueAsString(domain.getQualifications());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize qualifications for user {}", domain.getKey(), e);
        }

        String emergencyContact = "{}";
        if (domain.getEmergencyContact() != null) {
            try {
                emergencyContact = objectMapper.writeValueAsString(domain.getEmergencyContact());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize emergency contact for user {}", domain.getKey(), e);
            }
        }

        String address = "{}";
        if (domain.getAddress() != null) {
            try {
                address = objectMapper.writeValueAsString(domain.getAddress());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize address for user {}", domain.getKey(), e);
            }
        }

        return new EncryptedUserDetailsJpaEntity(
            domain.getKey().value(),
            domain.getAuthKey() != null ? domain.getAuthKey().value() : null,
            domain.getCreatedAt().toString(),
            domain.getUpdatedAt().toString(),
            domain.getVerifiedAt() != null ? domain.getVerifiedAt().toString() : null,
            domain.getLastLoginAt() != null ? domain.getLastLoginAt().toString() : null,
            domain.getGender() != null ? domain.getGender().value() : null,
            domain.getTitle() != null ? domain.getTitle().value() : null,
            domain.getFirstName().value(),
            domain.getNickName() != null ? domain.getNickName().value() : null,
            domain.getSecondName() != null ? domain.getSecondName().value() : null,
            domain.getLastName().value(),
            roles,
            qualifications,
            address,
            domain.getEmail() != null ? domain.getEmail().value() : null,
            domain.getPhone() != null ? domain.getPhone().value() : null,
            domain.getPhoneWork() != null ? domain.getPhoneWork().value() : null,
            domain.getMobile() != null ? domain.getMobile().value() : null,
            domain.getDateOfBirth() != null ? domain.getDateOfBirth().value() : null,
            domain.getPlaceOfBirth() != null ? domain.getPlaceOfBirth().value() : null,
            domain.getPassNr() != null ? domain.getPassNr().value() : null,
            domain.getComment() != null ? domain.getComment().value() : null,
            domain.getNationality() != null ? domain.getNationality().value() : null,
            emergencyContact,
            domain.getDiseases() != null ? domain.getDiseases().value() : null,
            domain.getIntolerances() != null ? domain.getIntolerances().value() : null,
            domain.getMedication() != null ? domain.getMedication().value() : null,
            domain.getDiet() != null ? domain.getDiet().value() : null
        );
    }

    public EncryptedUserDetails toDomain() {
        var roles = new Encrypted[] {};
        if (rolesRaw != null && rolesRaw.startsWith("[") && rolesRaw.endsWith("]")) {
            try {
                roles = objectMapper.readValue(rolesRaw, Encrypted[].class);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize roles for user {}", key, e);
            }
        }

        var qualifications = new EncryptedUserQualification[] {};
        if (qualificationsRaw != null && qualificationsRaw.startsWith("[") && qualificationsRaw.endsWith("]")) {
            try {
                qualifications = objectMapper.readValue(qualificationsRaw, EncryptedUserQualification[].class);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize qualifications for user {}", key, e);
            }
        }

        EncryptedEmergencyContact emergencyContact = null;
        if (emergencyContactRaw != null && emergencyContactRaw.startsWith("{") && emergencyContactRaw.endsWith("}")) {
            try {
                emergencyContact = objectMapper.readValue(emergencyContactRaw, EncryptedEmergencyContact.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize emergency contact for user {}", key, e);
            }
        }

        EncryptedAddress address = null;
        if (addressRaw != null && addressRaw.startsWith("{") && addressRaw.endsWith("}")) {
            try {
                address = objectMapper.readValue(addressRaw, EncryptedAddress.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize address for user {}", key, e);
            }
        }

        return new EncryptedUserDetails(
            new UserKey(key),
            authKey != null ? new AuthKey(authKey) : null,
            createdAt != null ? Instant.parse(createdAt) : Instant.now(),
            updatedAt != null ? Instant.parse(updatedAt) : Instant.now(),
            verifiedAt != null ? Instant.parse(verifiedAt) : null,
            lastLoginAt != null ? Instant.parse(lastLoginAt) : null,
            gender != null ? new Encrypted<>(gender) : null,
            title != null ? new Encrypted<>(title) : null,
            new Encrypted<>(firstName),
            nickName != null ? new Encrypted<>(nickName) : null,
            secondName != null ? new Encrypted<>(secondName) : null,
            new Encrypted<>(lastName),
            Arrays.stream(roles).map(it -> (Encrypted<Role>) it).toList(),
            Arrays.stream(qualifications).toList(),
            address,
            email != null ? new Encrypted<>(email) : null,
            phone != null ? new Encrypted<>(phone) : null,
            phoneWork != null ? new Encrypted<>(phoneWork) : null,
            mobile != null ? new Encrypted<>(mobile) : null,
            dateOfBirth != null ? new Encrypted<>(dateOfBirth) : null,
            placeOfBirth != null ? new Encrypted<>(placeOfBirth) : null,
            passNr != null ? new Encrypted<>(passNr) : null,
            comment != null ? new Encrypted<>(comment) : null,
            nationality != null ? new Encrypted<>(nationality) : null,
            emergencyContact,
            diseases != null ? new Encrypted<>(diseases) : null,
            intolerances != null ? new Encrypted<>(intolerances) : null,
            medication != null ? new Encrypted<>(medication) : null,
            diet != null ? new Encrypted<>(diet) : null
        );
    }
}