export interface AppSettings {
    email: EmailSettings;
    ui: UiSettings;
}

export interface UiSettings {
    menuTitle?: string;
    tabTitle?: string;
    technicalSupportEmail?: string;
    supportEmail?: string;
}

export interface EmailSettings {
    from?: string;
    fromDisplayName?: string;
    replyTo?: string;
    replyToDisplayName?: string;
    host?: string;
    port?: number;
    enableStartTls?: boolean;
    enableSSL?: boolean;
    username?: string;
    password?: string;
}
