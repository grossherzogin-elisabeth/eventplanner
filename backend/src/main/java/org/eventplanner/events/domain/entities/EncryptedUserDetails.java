package org.eventplanner.events.domain.entities;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import org.eventplanner.common.EncryptedString;
import org.eventplanner.events.domain.values.AuthKey;
import org.eventplanner.events.domain.values.EncryptedAddress;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class EncryptedUserDetails implements Serializable {
    private @NonNull UserKey key;
    private @Nullable AuthKey authKey;
    private @NonNull Instant createdAt;
    private @NonNull Instant updatedAt;
    private @Nullable Instant verifiedAt;
    private @Nullable Instant lastLoginAt;
    private @Nullable EncryptedString gender;
    private @Nullable EncryptedString title;
    private @NonNull EncryptedString firstName;
    private @Nullable EncryptedString nickName;
    private @Nullable EncryptedString secondName;
    private @NonNull EncryptedString lastName;
    private @NonNull List<EncryptedString> roles;
    private @NonNull List<EncryptedUserQualification> qualifications;
    private @Nullable EncryptedAddress address;
    private @Nullable EncryptedString email;
    private @Nullable EncryptedString phone;
    private @Nullable EncryptedString phoneWork;
    private @Nullable EncryptedString mobile;
    private @Nullable EncryptedString dateOfBirth;
    private @Nullable EncryptedString placeOfBirth;
    private @Nullable EncryptedString passNr;
    private @Nullable EncryptedString comment;
    private @Nullable EncryptedString nationality;
    private @Nullable EncryptedEmergencyContact emergencyContact;
    private @Nullable EncryptedString diseases;
    private @Nullable EncryptedString intolerances;
    private @Nullable EncryptedString medication;
    private @Nullable EncryptedString diet;
}
