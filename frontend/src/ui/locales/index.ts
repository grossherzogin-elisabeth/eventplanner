import de from './de.yaml';

export type Locale = 'de';
export type Messages = typeof de;

export const locales: Record<Locale, Messages> = {
    de,
};
