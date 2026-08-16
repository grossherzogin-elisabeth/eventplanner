import { describe, expect, it } from 'vitest';
import type { Locale } from '@/ui/locales';
import { locales } from '@/ui/locales';

describe('locales', () => {
    const defaultLocale = import.meta.env.VITE_I18N_FALLBACK_LOCALE as Locale;
    const defaultLocaleKeys = flattenKeys(locales[defaultLocale]);
    const i18nKeysUsedInSource = findI18nKeysUsedInSource();
    const whitelistedKeys = [
        // add exceptions here for unused i18n keys, e.g. when used with concatenation
        'domain.*.validation.*', // used in services
        'generic.validation.*', // used in services
        'generic.*',
        'domain.*', // TODO remove this exception after i18n is complete
    ].map((it) => transformToRegex(it));

    it('should load plausible key counts', () => {
        console.log(`Found ${defaultLocaleKeys.size} unique i18n messages in default locale`);
        console.log(`Found ${i18nKeysUsedInSource.size} unique i18n keys used in the source code`);
        // Safety net, to ensure we still find a realistic amount of i18n keys used. At the time of
        // writing this test, the default locale has 400+ keys, so when we drop >25% this is a sign that
        // probably something is wrong with the i18n setup or the test itself
        expect(defaultLocale).toBeDefined();
        expect(defaultLocaleKeys.size).toBeGreaterThan(300);
        expect(i18nKeysUsedInSource.size).toBeGreaterThan(300);
    });

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

    it('should not miss any localization key used in source code', async () => {
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

        const usedKeys = new Set<string>();
        for (const source of sourceModules) {
            const matches: string[] = [];
            const functionRegex = /[$ .](?:t|tc|te|tm)\(\s*['"`]([^'"`]+)['"`]/g;
            for (const match of source.matchAll(functionRegex)) {
                matches.push(match[1]);
            }
            const componentRegex = /<i18n-t.*keypath=['"`]*([^'"`]+)['"`]/g;
            for (const match of source.matchAll(componentRegex)) {
                matches.push(match[1]);
            }
            for (const match of matches) {
                if (match.includes('${')) {
                    const prefix = match.substring(0, match.indexOf('${'));
                    usedKeys.add(prefix + '*');
                } else if (match.endsWith('.')) {
                    usedKeys.add(match + '*');
                } else {
                    usedKeys.add(match);
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
