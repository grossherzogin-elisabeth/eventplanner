import type { SettingsRepository } from '@/application/ports';
import type { Config } from '@/application/values/Config';

export const defaultConfig: Config = {
    baseUrl: import.meta.env.BASE_URL,
    i18nLocale: import.meta.env.VITE_I18N_LOCALE || 'de',
    i18nAvailableLocales: (import.meta.env.VITE_I18N_LOCALES || 'de').split(','),
    i18nFallbackLocale: import.meta.env.VITE_I18N_FALLBACK_LOCALE || 'de',
    overrideSignedInUserKey: localStorage.getItem('eventplanner.overrideSignedInUserKey') || undefined,
    menuTitle: 'Reiseplaner',
    tabTitle: 'Reiseplaner',
    technicalSupportEmail: 'support@example.de',
    supportEmail: 'support@example.de',

    // feature flags
    enableEventAdminListPositionsOverview: import.meta.env.VITE_ENABLE_EVENT_ADMIN_LIST_POSITIONS_OVERVIEW === 'true',
    enableEventAdminListPreviewSheet: import.meta.env.VITE_ENABLE_EVENT_ADMIN_LIST_PREVIEW_SHEET === 'true',
    enableTableActionsButtonMobile: import.meta.env.VITE_ENABLE_TABLE_ACTIONS_BUTTON_MOBILE === 'true',
    enableGravatar: import.meta.env.VITE_ENABLE_GRAVATAR === 'true',
};

if (import.meta.env.MODE === 'development') {
    console.log('Loading environment configuration');
    console.log(import.meta.env);
}

export class ConfigService {
    private readonly settingsRepository: SettingsRepository;
    private readonly config: Config;
    private readonly init: Promise<void>;

    constructor(params: { settingsRepository: SettingsRepository }) {
        this.settingsRepository = params.settingsRepository;
        this.config = defaultConfig;
        this.init = this.initialize();
    }

    public async initialization(): Promise<void> {
        return this.init;
    }

    private async initialize(): Promise<void> {
        this.readFeatureFlag('enableEventAdminListPositionsOverview');
        this.readFeatureFlag('enableEventAdminListPreviewSheet');
        this.readFeatureFlag('enableTableActionsButtonMobile');
        this.readFeatureFlag('enableGravatar');
        this.loadCachedServerConfig();
        try {
            await this.fetchServerConfig();
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
        } catch (e) {
            console.warn('Failed to fetch config, continuing with local data');
        }
    }

    public getConfig(): Config {
        return this.config;
    }

    private async fetchServerConfig(): Promise<void> {
        console.log('📡 Fetching config');
        const serverConfig = await this.settingsRepository.readConfig();
        if (serverConfig.menuTitle) {
            this.config.menuTitle = serverConfig.menuTitle;
        }
        if (serverConfig.tabTitle) {
            this.config.tabTitle = serverConfig.tabTitle;
        }
        if (serverConfig.supportEmail) {
            this.config.supportEmail = serverConfig.supportEmail;
        }
        if (serverConfig.technicalSupportEmail) {
            this.config.technicalSupportEmail = serverConfig.technicalSupportEmail;
        }
        this.cacheServerConfig(this.config);
    }

    private cacheServerConfig(config: Config): void {
        localStorage.setItem(
            'config',
            JSON.stringify({
                menuTitle: config.menuTitle,
                tabTitle: config.tabTitle,
                supportEmail: config.supportEmail,
                technicalSupportEmail: config.technicalSupportEmail,
            })
        );
    }

    private loadCachedServerConfig(): void {
        try {
            const cached = JSON.parse(localStorage.getItem('config') ?? '{}');
            if (cached.menuTitle) {
                this.config.menuTitle = cached.menuTitle;
            }
            if (cached.tabTitle) {
                this.config.tabTitle = cached.tabTitle;
            }
            if (cached.supportEmail) {
                this.config.supportEmail = cached.supportEmail;
            }
            if (cached.technicalSupportEmail) {
                this.config.technicalSupportEmail = cached.technicalSupportEmail;
            }
        } catch (e) {
            console.error('Failed to load stored config', e);
        }
    }

    private readFeatureFlag(name: keyof Config): void {
        const flag = localStorage.getItem(name);
        if (flag === 'true') {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (this.config as any)[name] = true;
        } else if (flag === 'false') {
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (this.config as any)[name] = false;
        }
    }
}
