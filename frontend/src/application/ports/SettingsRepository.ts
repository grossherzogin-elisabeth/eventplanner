import type { AppSettings, UiSettings } from '@/domain';

export interface SettingsRepository {
    readConfig(): Promise<UiSettings>;
    readAdminSettings(): Promise<AppSettings>;
    writeSettings(settings: AppSettings): Promise<AppSettings>;
}
