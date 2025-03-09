package org.eventplanner.events.domain.entities.users;

import java.io.Serializable;

import org.eventplanner.common.EncryptedString;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public class EncryptedUserEmergencyContact implements Serializable {
    private @NonNull EncryptedString name;
    private @NonNull EncryptedString phone;
}
