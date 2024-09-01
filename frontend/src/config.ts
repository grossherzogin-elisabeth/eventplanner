import type { Config } from '@/application';

export const config: Config = {
    baseUrl: import.meta.env.BASE_URL,
    buildType: import.meta.env.VITE_BUILD_TYPE || 'localhost',
    i18nLocale: import.meta.env.VITE_I18N_LOCALE || 'de',
    i18nAvailableLocales: (import.meta.env.VITE_I18N_LOCALES || 'de').split(','),
    i18nFallbackLocale: import.meta.env.VITE_I18N_FALLBACK_LOCALE || 'de',
    authLoginEndpoint: import.meta.env.VITE_AUTH_LOGIN_ENDPOINT || '',
    authLogoutEndpoint: import.meta.env.VITE_AUTH_LOGOUT_ENDPOINT || '',
    overrideSignedInUserKey: localStorage.getItem('eventplanner.overrideSignedInUserKey') || undefined,
    askBeforeLogin: import.meta.env.VITE_AUTH_ASK_BEFORE_LOGIN === 'true',
};

export default config;
