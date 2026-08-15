import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { SettingsRepository } from '@/application/ports';
import { ConfigService } from '@/application/services';
import { Theme } from '@/domain';
import { CAPTAIN } from '~/mocks';

async function flushPromises(): Promise<void> {
    await new Promise((resolve) => setTimeout(resolve, 0));
}

describe('ConfigService', () => {
    let testee: ConfigService;
    let settingsRepository: SettingsRepository;

    beforeEach(() => {
        settingsRepository = {
            readConfig: vi.fn(async () => ({})),
        } as unknown as SettingsRepository;
    });

    it('should load cached config and then apply server overrides', async () => {
        localStorage.setItem(
            'config',
            JSON.stringify({
                menuTitle: 'Cached Menu',
                tabTitle: 'Cached Tab',
                supportEmail: 'cached@example.com',
                technicalSupportEmail: 'cached-tech@example.com',
            })
        );
        settingsRepository.readConfig = vi.fn(async () => ({
            menuTitle: 'Server Menu',
            supportEmail: 'server@example.com',
        }));

        testee = new ConfigService({ settingsRepository });
        await flushPromises();

        const config = testee.getConfig();
        expect(config.menuTitle).toBe('Server Menu');
        expect(config.supportEmail).toBe('server@example.com');
        expect(config.tabTitle).toBe('Cached Tab');
        expect(config.technicalSupportEmail).toBe('cached-tech@example.com');

        const stored = JSON.parse(localStorage.getItem('config') || '{}') as Record<string, string>;
        expect(stored.menuTitle).toBe('Server Menu');
        expect(stored.supportEmail).toBe('server@example.com');
    });

    it('should keep local/default values when fetching server config fails', async () => {
        localStorage.setItem('config', JSON.stringify({ menuTitle: 'Cached Title' }));
        settingsRepository.readConfig = vi.fn(async () => {
            throw new Error('offline');
        });

        testee = new ConfigService({ settingsRepository });
        await flushPromises();

        expect(testee.getConfig().menuTitle).toBe('Cached Title');
        expect(testee.getConfig().tabTitle).toBeDefined();
    });

    it('should ignore broken cached json and still initialize config', async () => {
        localStorage.setItem('config', '{not-json');
        testee = new ConfigService({ settingsRepository });
        await flushPromises();

        expect(testee.getConfig().menuTitle).toBeDefined();
        expect(settingsRepository.readConfig).toHaveBeenCalledTimes(1);
    });

    describe('getUserSettings', () => {
        it('should return default system theme when no settings are stored', async () => {
            const result = testee.getUserSettings();

            expect(result.theme).toBe(Theme.System);
        });

        it('should return stored settings from localStorage', async () => {
            localStorage.setItem('settings', JSON.stringify({ theme: Theme.Dark, preferredPosition: CAPTAIN }));

            const result = testee.getUserSettings();

            expect(result.theme).toBe(Theme.Dark);
            expect(result.preferredPosition).toBe(CAPTAIN);
        });
    });

    describe('saveUserSettings', () => {
        it('should persist merged settings to localStorage', async () => {
            testee.saveUserSettings({ theme: Theme.Dark });

            const stored = JSON.parse(localStorage.getItem('settings') ?? '{}');
            expect(stored.theme).toBe(Theme.Dark);
        });

        it('should merge patch with existing settings', async () => {
            localStorage.setItem('settings', JSON.stringify({ preferredPosition: CAPTAIN }));

            const result = testee.saveUserSettings({ theme: Theme.Light });

            expect(result.theme).toBe(Theme.Light);
            expect(result.preferredPosition).toBe(CAPTAIN);
        });
    });

    describe('applyUserSettings', () => {
        it('should add dark class to html element when theme is dark', async () => {
            testee = new ConfigService({ settingsRepository });
            testee.saveUserSettings({ theme: Theme.Dark });

            expect(document.querySelector('html')?.classList.contains('dark')).toBe(true);
        });

        it('should remove dark class from html element when theme is light', async () => {
            document.querySelector('html')?.classList.add('dark');

            testee = new ConfigService({ settingsRepository });
            testee.saveUserSettings({ theme: Theme.Light });

            expect(document.querySelector('html')?.classList.contains('dark')).toBe(false);
        });

        it('should apply dark class when theme is system and system prefers dark', async () => {
            vi.stubGlobal(
                'matchMedia',
                vi.fn(() => ({ matches: true, addEventListener: vi.fn(), removeEventListener: vi.fn() }))
            );

            testee = new ConfigService({ settingsRepository });
            testee.saveUserSettings({ theme: Theme.System });

            expect(document.querySelector('html')?.classList.contains('dark')).toBe(true);
        });
    });
});
