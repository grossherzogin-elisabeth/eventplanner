package org.eventplanner.events.domain.entities.users;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eventplanner.common.StringUtils;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import freemarker.ext.beans.TemplateAccessible;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@Getter
@Setter
@With
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class User {
    private @NonNull UserKey key;
    private @NonNull String firstName;
    private @NonNull String lastName;
    private @Nullable String nickName;

    /**
     * Returns the nickname of the user if present, or the first name otherwise
     *
     * @return the first name of the user
     */
    @TemplateAccessible
    public @NonNull String getDisplayName() {
        if (nickName != null && !nickName.isEmpty()) {
            return nickName;
        }
        return firstName;
    }

    @TemplateAccessible
    public @NonNull String getFullName() {
        return combineNames(getDisplayName(), lastName);
    }

    public static @NonNull String combineNames(@NonNull String... names) {
        return Stream.of(names)
            .filter(s -> !StringUtils.isBlank(s))
            .map(String::trim)
            .collect(Collectors.joining(" "));
    }
}
