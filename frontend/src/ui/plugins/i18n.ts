import type { DateTimeOptions, I18n } from 'vue-i18n';
import { createI18n } from 'vue-i18n';
import type { Config } from '@/application/values/Config';
import { DateTimeFormat } from '@/common/date';
import messagesDe from '@/ui/locales/de.yml';

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
    weekday: 'short',
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

export function setupI18n(config: Config): I18n {
    return createI18n({
        legacy: false, // required to enable useI18n in Vue setup script
        locale: config.i18nLocale,
        fallbackLocale: config.i18nFallbackLocale,
        messages: {
            de: messagesDe,
        },
        availableLocales: config.i18nAvailableLocales,
        silentFallbackWarn: true,
        silentTranslationWarn: true,
        missingWarn: false,
        fallbackWarn: false,
        datetimeFormats: {
            de: datetimeFormatsDe,
        },
    });
}
