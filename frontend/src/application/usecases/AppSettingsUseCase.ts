import type { ErrorHandlingService, NotificationService, SettingsRepository } from '@/application';
import type { AppSettings } from '@/domain';

export class AppSettingsUseCase {
    private readonly notificationService: NotificationService;
    private readonly errorHandlingService: ErrorHandlingService;
    private readonly settingsRepository: SettingsRepository;

    constructor(params: {
        notificationService: NotificationService;
        errorHandlingService: ErrorHandlingService;
        settingsRepository: SettingsRepository;
    }) {
        this.notificationService = params.notificationService;
        this.errorHandlingService = params.errorHandlingService;
        this.settingsRepository = params.settingsRepository;
    }

    public async getAdminSettings(): Promise<AppSettings> {
        try {
            return this.settingsRepository.readAdminSettings();
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async updateSettings(settings: AppSettings): Promise<AppSettings> {
        try {
            const savedSettings = await this.settingsRepository.writeSettings(settings);
            this.notificationService.success('Einstellungen gespeichert');
            return savedSettings;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }
}
