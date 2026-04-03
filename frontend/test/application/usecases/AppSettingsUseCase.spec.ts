import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { SettingsRepository } from '@/application/ports';
import type { ErrorHandlingService, NotificationService } from '@/application/services';
import { AppSettingsUseCase } from '@/application/usecases/AppSettingsUseCase';
import { mockSettings } from '~/mocks';

describe('AppSettingsUseCase', () => {
    let testee: AppSettingsUseCase;
    let settingsRepository: SettingsRepository;
    let errorHandlingService: ErrorHandlingService;
    let notificationService: NotificationService;

    beforeEach(() => {
        settingsRepository = {
            readAdminSettings: vi.fn(async () => mockSettings()),
            writeSettings: vi.fn(async (settings) => settings),
        } as unknown as SettingsRepository;
        errorHandlingService = { handleRawError: vi.fn() } as unknown as ErrorHandlingService;
        notificationService = { success: vi.fn() } as unknown as NotificationService;
        testee = new AppSettingsUseCase({ settingsRepository, errorHandlingService, notificationService });
    });

    it('should read admin settings', async () => {
        const settings = await testee.getAdminSettings();

        expect(settingsRepository.readAdminSettings).toHaveBeenCalledOnce();
        expect(settings).toEqual(mockSettings());
    });

    it('should handle read errors and rethrow', async () => {
        const error = new Error('failed to read settings');
        settingsRepository.readAdminSettings = vi.fn(async () => {
            throw error;
        });

        try {
            await testee.getAdminSettings();
            expect(true).toBe(false); // fail test when promise was not rejected
        } catch (e) {
            expect(e).toBe(error);
        }
        expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
    });

    it('should update settings and show success notification', async () => {
        const settings = mockSettings({ notifications: { teamsWebhookUrl: 'https://new-teams-webhook.example' } });
        settingsRepository.writeSettings = vi.fn(async () => settings);

        const updatedSettings = await testee.updateSettings(settings);

        expect(settingsRepository.writeSettings).toHaveBeenCalledWith(settings);
        expect(notificationService.success).toHaveBeenCalled();
        expect(updatedSettings).toEqual(settings);
    });

    it('should handle update errors and rethrow', async () => {
        const settings = mockSettings();
        const error = new Error('failed to update settings');
        settingsRepository.writeSettings = vi.fn(async () => {
            throw error;
        });

        try {
            await testee.updateSettings(settings);
            expect(true).toBe(false); // fail test when promise was not rejected
        } catch (e) {
            expect(e).toBe(error);
        }
        expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
        expect(notificationService.success).not.toHaveBeenCalled();
    });
});
