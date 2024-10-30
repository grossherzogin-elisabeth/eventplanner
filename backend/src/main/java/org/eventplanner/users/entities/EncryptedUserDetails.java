package org.eventplanner.users.entities;

import lombok.*;
import org.eventplanner.users.values.EncryptedAddress;
import org.eventplanner.common.EncryptedString;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
public class EncryptedUserDetails implements Serializable {
    private @NonNull UserKey key;
    private @Nullable EncryptedString authKey;
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
    private @Nullable EncryptedString mobile;
    private @Nullable EncryptedString dateOfBirth;
    private @Nullable EncryptedString placeOfBirth;
    private @Nullable EncryptedString passNr;
    private @Nullable EncryptedString comment;
    private @Nullable EncryptedString nationality;
}
