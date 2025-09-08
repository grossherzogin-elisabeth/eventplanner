package org.eventplanner.events.domain.values.config;

import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.With;

@With
public record FrontendConfig(
    @Nullable String menuTitle,
    @Nullable String tabTitle,
    @Nullable String technicalSupportEmail,
    @Nullable String supportEmail,
    @Nullable String url
) {
    public FrontendConfig() {
        this(null, null, null, null, null);
    }

    public @NonNull FrontendConfig apply(@NonNull final FrontendConfig other) {
        return new FrontendConfig(
            other.menuTitle != null ? other.menuTitle : menuTitle,
            other.tabTitle != null ? other.tabTitle : tabTitle,
            other.technicalSupportEmail != null ? other.technicalSupportEmail : technicalSupportEmail,
            other.supportEmail != null ? other.supportEmail : supportEmail,
            other.url != null ? other.url : url
        );
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static final class UpdateSpec {
        private @Nullable String menuTitle;
        private @Nullable String tabTitle;
        private @Nullable String technicalSupportEmail;
        private @Nullable String supportEmail;

        public @NonNull UpdateSpec clearUnchanged(@NonNull final FrontendConfig current) {
            if (Objects.equals(menuTitle, current.menuTitle)) {
                menuTitle = null;
            }
            if (Objects.equals(tabTitle, current.tabTitle)) {
                tabTitle = null;
            }
            if (Objects.equals(technicalSupportEmail, current.technicalSupportEmail)) {
                technicalSupportEmail = null;
            }
            if (Objects.equals(supportEmail, current.supportEmail)) {
                supportEmail = null;
            }
            return this;
        }
    }
}
