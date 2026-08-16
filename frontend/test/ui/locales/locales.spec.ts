import { describe, expect, it } from 'vitest';
import type { Locale } from '@/ui/locales';
import { locales } from '@/ui/locales';

describe('locales', () => {
    const defaultLocale = import.meta.env.VITE_I18N_FALLBACK_LOCALE as Locale;
    const defaultLocaleKeys = flattenKeys(locales[defaultLocale]);
    const i18nKeysUsedInSource = findI18nKeysUsedInSource();
    const whitelistedKeys = [
        // add exceptions here for unused i18n keys, e.g. when used with concatenation
        'generic.*',
        'domain.*',
        'views.*', // TODO remove this exception after cleanup
        'components.*', // TODO remove this exception after cleanup
    ].map((it) => transformToRegex(it));

    it.each(Object.keys(locales))('$0 should have same keys as the default locale', (locale) => {
        const localeKeys = flattenKeys(locales[locale as Locale]);
        expect(localeKeys).toEqual(defaultLocaleKeys);
    });

    it('should not have unused localization keys', async () => {
        const usedPatterns = [...i18nKeysUsedInSource].filter((it) => it.endsWith('*')).map((it) => transformToRegex(it));
        const unused = [...defaultLocaleKeys]
            .filter((k) => !whitelistedKeys.some((pattern) => pattern.test(k)))
            .filter((k) => !usedPatterns.some((pattern) => pattern.test(k)))
            .filter((k) => !i18nKeysUsedInSource.has(k));
        expect(unused).toEqual([]);
    });

    it('should not miss any localization key used in source', async () => {
        const missingKeys = new Set<string>();
        [...i18nKeysUsedInSource]
            .filter((it) => it.endsWith('*'))
            .filter((it) => ![...defaultLocaleKeys].some((k) => transformToRegex(it).test(k)))
            .forEach((it) => missingKeys.add(it));
        [...i18nKeysUsedInSource]
            .filter((it) => !it.endsWith('*'))
            .filter((it) => !defaultLocaleKeys.has(it))
            .forEach((it) => missingKeys.add(it));
        expect([...missingKeys]).toEqual([]);
    });

    function findI18nKeysUsedInSource(): Set<string> {
        const sourceModules = Object.values(
            import.meta.glob('@/**/*.{vue,ts}', {
                eager: true,
                query: '?raw',
                import: 'default',
            }) as Record<string, string>
        );
        expect(sourceModules).not.toHaveLength(0);

        const usedKeys = new Set<string>();
        for (const source of sourceModules) {
            const regex = /[$ .](?:t|tc|te|tm)\(\s*['"`]([^'"`]+)['"`]/g;
            for (const match of source.matchAll(regex)) {
                const key = match[1];
                if (key.includes('${')) {
                    const prefix = key.substring(0, key.indexOf('${'));
                    usedKeys.add(prefix + '*');
                } else if (key.endsWith('.')) {
                    usedKeys.add(key + '*');
                } else {
                    usedKeys.add(key);
                }
            }
        }
        return usedKeys;
    }

    function transformToRegex(pattern: string): RegExp {
        const escaped = pattern.replace(/[\\^$.*+?()[\]{}|]/g, '\\$&');
        const regex = `^${escaped.replace(/\\\*/g, '.*')}$`;
        return new RegExp(regex);
    }

    function flattenKeys(locale: Record<string, unknown>, prefix: string = ''): Set<string> {
        const keys = new Set<string>();
        for (const [key, value] of Object.entries(locale)) {
            const path = prefix ? `${prefix}.${key}` : key;
            if (typeof value === 'object' && value !== null) {
                flattenKeys(value as Record<string, unknown>, path).forEach((k) => keys.add(k));
            } else {
                keys.add(path);
            }
        }
        return keys;
    }
});
