import type { SettingsRepresentation } from '@/adapter/rest/SettingsRestRepository.ts';

export function mockSettingsRepresentation(overwrite?: Partial<SettingsRepresentation>): SettingsRepresentation {
    const settings: SettingsRepresentation = {
        ui: {
            tabTitle: 'tab title',
            menuTitle: 'menu title',
            supportEmail: 'support@email.com',
            technicalSupportEmail: 'tech.support@email.com',
        },
        notifications: {
            teamsWebhookUrl: 'https://teams-webhook.com',
        },
        email: {
            from: 'from@email.com',
            fromDisplayName: 'from',
            replyTo: 'reply.to@email.com',
            replyToDisplayName: 'reply to',
            host: 'smtp.office365.com',
            port: 587,
            enableStartTls: false,
            enableSSL: false,
            username: 'from@email.com',
        },
    };
    return overwrite ? Object.assign(settings, overwrite) : settings;
}
