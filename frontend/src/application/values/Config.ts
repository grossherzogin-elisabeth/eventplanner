/**
 * This type contains the application config.
 */
export interface Config {
    buildType: 'prod' | 'stage' | 'dev' | 'localhost';
    baseUrl: string;

    // i18n
    i18nLocale: string;
    i18nFallbackLocale: string;
    i18nAvailableLocales: string[];

    // service urls
    serviceUrl: string;
    authLoginEndpoint: string;
    authLogoutEndpoint: string;

    overrideSignedInUserKey?: string;
    askBeforeLogin?: boolean;
}
