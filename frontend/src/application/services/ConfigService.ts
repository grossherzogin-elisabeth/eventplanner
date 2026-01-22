import type { SettingsRepository } from '@/application/ports';
import type { Config } from '@/application/values/Config';

export const defaultConfig: Config = {
    baseUrl: import.meta.env.BASE_URL,
    buildType: import.meta.env.VITE_BUILD_TYPE || 'localhost',
    i18nLocale: import.meta.env.VITE_I18N_LOCALE || 'de',
    i18nAvailableLocales: (import.meta.env.VITE_I18N_LOCALES || 'de').split(','),
    i18nFallbackLocale: import.meta.env.VITE_I18N_FALLBACK_LOCALE || 'de',
    overrideSignedInUserKey: localStorage.getItem('eventplanner.overrideSignedInUserKey') || undefined,
    askBeforeLogin: import.meta.env.VITE_AUTH_ASK_BEFORE_LOGIN === 'true',
    menuTitle: 'Reiseplaner',
    tabTitle: 'Reiseplaner',
    technicalSupportEmail: 'support@example.de',
    supportEmail: 'support@example.de',
};

export class ConfigService {
    private readonly settingsRepository: SettingsRepository;
    private readonly config: Config;

    constructor(params: { settingsRepository: SettingsRepository }) {
        this.settingsRepository = params.settingsRepository;
        this.config = defaultConfig;
        console.log('🚀 Initializing ConfigService');
        this.initialize();
    }

    private async initialize(): Promise<void> {
        this.loadStoredConfig();
        try {
            await this.fetchConfig();
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
        } catch (e) {
            console.warn('Failed to fetch config, continuing with local data');
        }
    }

    public getConfig(): Config {
        return this.config;
    }

    private async fetchConfig(): Promise<void> {
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
        this.storeConfig(this.config);
    }

    private storeConfig(config: Config): void {
        localStorage.setItem('config', JSON.stringify(config));
    }

    private loadStoredConfig(): void {
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
}
