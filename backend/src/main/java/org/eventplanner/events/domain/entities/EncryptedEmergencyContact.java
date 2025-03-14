package org.eventplanner.events.domain.entities;

import java.io.Serializable;

import org.eventplanner.common.Encrypted;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@NoArgsConstructor
@ToString
public class EncryptedEmergencyContact implements Serializable {
    private @NonNull Encrypted<String> name;
    private @NonNull Encrypted<String> phone;
}
