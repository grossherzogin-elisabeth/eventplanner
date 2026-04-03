import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { SettingsRepository } from '@/application/ports';
import { ConfigService } from '@/application/services';

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
});
