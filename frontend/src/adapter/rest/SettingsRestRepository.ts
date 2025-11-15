import { getCsrfToken } from '@/adapter/util/Csrf';
import type { SettingsRepository } from '@/application';
import type { AppSettings, UiSettings } from '@/domain';

export interface UiSettingsRepresentation {
    menuTitle?: string;
    tabTitle?: string;
    technicalSupportEmail?: string;
    supportEmail?: string;
}

export interface SettingsRepresentation {
    ui: UiSettingsRepresentation;
    notifications: {
        teamsWebhookUrl?: string;
    };
    email: {
        from?: string;
        fromDisplayName?: string;
        replyTo?: string;
        replyToDisplayName?: string;
        host?: string;
        port?: number;
        enableStartTls?: boolean;
        enableSSL?: boolean;
        username?: string;
    };
}

export interface UpdateSettingsRequest {
    ui: UiSettingsRepresentation;
    notifications: {
        teamsWebhookUrl?: string;
    };
    email: {
        from?: string;
        fromDisplayName?: string;
        replyTo?: string;
        replyToDisplayName?: string;
        host?: string;
        port?: number;
        enableStartTls?: boolean;
        enableSSL?: boolean;
        username?: string;
        password?: string;
    };
}

export class SettingsRestRepository implements SettingsRepository {
    private static mapToDomain(representation: SettingsRepresentation): AppSettings {
        return {
            notifications: {
                teamsWebhookUrl: representation.notifications.teamsWebhookUrl,
            },
            email: {
                from: representation.email.from,
                fromDisplayName: representation.email.fromDisplayName,
                replyTo: representation.email.replyTo,
                replyToDisplayName: representation.email.replyToDisplayName,
                host: representation.email.host,
                port: representation.email.port,
                enableStartTls: representation.email.enableStartTls,
                enableSSL: representation.email.enableSSL,
                username: representation.email.username,
                password: undefined,
            },
            ui: {
                tabTitle: representation.ui.tabTitle,
                menuTitle: representation.ui.menuTitle,
                supportEmail: representation.ui.supportEmail,
                technicalSupportEmail: representation.ui.technicalSupportEmail,
            },
        };
    }

    public async readConfig(): Promise<UiSettings> {
        const response = await fetch('/api/v1/config', { credentials: 'include' });
        if (response.ok) {
            const representation = (await response.clone().json()) as UiSettingsRepresentation;
            return {
                menuTitle: representation.menuTitle,
                tabTitle: representation.tabTitle,
                technicalSupportEmail: representation.technicalSupportEmail,
                supportEmail: representation.supportEmail,
            };
        } else {
            throw response;
        }
    }

    public async readAdminSettings(): Promise<AppSettings> {
        const response = await fetch('/api/v1/settings', { credentials: 'include' });
        if (response.ok) {
            const representation = (await response.clone().json()) as SettingsRepresentation;
            return SettingsRestRepository.mapToDomain(representation);
        } else {
            throw response;
        }
    }

    public async writeSettings(settings: AppSettings): Promise<AppSettings> {
        const requestBody: UpdateSettingsRequest = {
            notifications: {
                teamsWebhookUrl: settings.notifications.teamsWebhookUrl,
            },
            email: {
                from: settings.email.from,
                fromDisplayName: settings.email.fromDisplayName,
                replyTo: settings.email.replyTo,
                replyToDisplayName: settings.email.replyToDisplayName,
                host: settings.email.host,
                port: settings.email.port,
                enableStartTls: settings.email.enableStartTls,
                enableSSL: settings.email.enableSSL,
                username: settings.email.username,
                password: settings.email.password,
            },
            ui: {
                tabTitle: settings.ui.tabTitle,
                menuTitle: settings.ui.menuTitle,
                supportEmail: settings.ui.supportEmail,
                technicalSupportEmail: settings.ui.technicalSupportEmail,
            },
        };

        const response = await fetch('/api/v1/settings', {
            method: 'PUT',
            credentials: 'include',
            body: JSON.stringify(requestBody),
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken(),
            },
        });
        if (response.ok) {
            const representation = (await response.clone().json()) as SettingsRepresentation;
            return SettingsRestRepository.mapToDomain(representation);
        } else {
            throw response;
        }
    }
}
