import type { DateTimeOptions, I18n } from 'vue-i18n';
import { createI18n } from 'vue-i18n';
import { DateTimeFormat } from '@/common/date';
import { locales } from '@/ui/locales';

const datetimeFormatsDe: Record<string, DateTimeOptions> = {};
datetimeFormatsDe[DateTimeFormat.DD_MM_YYYY] = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
};
datetimeFormatsDe[DateTimeFormat.DDD] = {
    weekday: 'short',
};
datetimeFormatsDe[DateTimeFormat.DD_MM] = {
    month: '2-digit',
    day: '2-digit',
};
datetimeFormatsDe[DateTimeFormat.DDD_DD_MM] = {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
};
datetimeFormatsDe[DateTimeFormat.DDD_DD_MM_YYYY] = {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
};
datetimeFormatsDe[DateTimeFormat.MMMM_YYYY] = {
    month: 'long',
    year: 'numeric',
};
datetimeFormatsDe[DateTimeFormat.MMM] = {
    month: 'short',
};
datetimeFormatsDe[DateTimeFormat.hh_mm] = {
    hour: '2-digit',
    minute: '2-digit',
};
datetimeFormatsDe[DateTimeFormat.DDD_DD_MM_hh_mm] = {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
};

export function setupI18n(config: { locale: string; fallbackLocale: string; availableLocales: string[]; throwOnMissing?: boolean }): I18n {
    return createI18n({
        legacy: false, // required to enable useI18n in Vue setup script
        locale: config.locale,
        fallbackLocale: config.fallbackLocale,
        messages: locales,
        availableLocales: config.availableLocales,
        silentFallbackWarn: true,
        silentTranslationWarn: true,
        missingWarn: true,
        missing: (locale, key) => {
            if (config.throwOnMissing) {
                // mainly for testing: prevent usage of non-existing i18n keys by letting tests fail
                const message = `Localization key '${key}' is missing for locale ${locale}`;
                console.error(message);
                throw new Error(message);
            }
        },
        fallbackWarn: false,
        datetimeFormats: {
            de: datetimeFormatsDe,
        },
    });
}
