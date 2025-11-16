import type { UiSettingsRepresentation } from '@/adapter/rest/SettingsRestRepository';

export function mockConfigRepresentation(): UiSettingsRepresentation {
    return {
        menuTitle: 'Test Menu',
        tabTitle: 'Test',
        technicalSupportEmail: 'tech-support@email.com',
        supportEmail: 'support@email.com',
    };
}
