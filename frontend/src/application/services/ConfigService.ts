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
        this.loadCachedConfig();
        this.loadServerConfig();
    }

    public getConfig(): Config {
        return this.config;
    }

    private loadCachedConfig(): void {
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
            console.error('Failed to load cached config', e);
        }
    }

    private cacheConfig(config: Config): void {
        localStorage.setItem('config', JSON.stringify(config));
    }

    private async loadServerConfig(): Promise<void> {
        try {
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
            this.cacheConfig(this.config);
        } catch (e) {
            console.error('Failed to load server config', e);
        }
    }
}
