/**
 * This type contains the application config.
 */
export interface Config {
    baseUrl: string;

    // i18n
    i18nLocale: string;
    i18nFallbackLocale: string;
    i18nAvailableLocales: string[];

    overrideSignedInUserKey?: string;

    // config from service
    menuTitle: string;
    tabTitle: string;
    supportEmail: string;
    technicalSupportEmail: string;

    // feature flags
    enableEventAdminListPositionsOverview?: boolean;
    enableEventAdminListPreviewSheet?: boolean;
    enableTableActionsButtonMobile?: boolean;
    enableGravatar?: boolean;
}
