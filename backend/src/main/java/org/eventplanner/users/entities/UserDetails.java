package org.eventplanner.users.entities;

import lombok.*;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.Address;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.Role;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDetails {
    private final @NonNull UserKey key;
    private @Nullable AuthKey authKey;
    private @Nullable String title;
    private @NonNull String firstName;
    private @Nullable String secondName;
    private @NonNull String lastName;
    private @NonNull List<PositionKey> positions = new LinkedList<>();
    private @NonNull List<Role> roles = new LinkedList<>();
    private @NonNull List<UserQualification> qualifications = new LinkedList<>();
    private @Nullable Address address;
    private @Nullable String email;
    private @Nullable String phone;
    private @Nullable String mobile;
    private @Nullable ZonedDateTime dateOfBirth;
    private @Nullable String placeOfBirth;
    private @Nullable String passNr;
    private @Nullable String comment;

    public @NonNull String getFullName() {
        StringBuilder stb = new StringBuilder();
        if (title != null) {
            stb.append(title).append(" ");
        }
        stb.append(firstName).append(" ");
        if (secondName != null) {
            stb.append(secondName).append(" ");
        }
        stb.append(lastName);
        return stb.toString();
    }

    public @NonNull User cropToUser() {
        return new User(key, firstName, lastName, positions);
    }
}
