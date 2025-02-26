package org.eventplanner.domain.entities;

import java.io.Serializable;

import org.eventplanner.common.EncryptedString;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
public class EncryptedEmergencyContact implements Serializable {
    private @NonNull EncryptedString name;
    private @NonNull EncryptedString phone;
}
