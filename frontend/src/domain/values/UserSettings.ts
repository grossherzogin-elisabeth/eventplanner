import type { PositionKey } from '@/domain';

export interface UserSettings {
    preferredPosition?: PositionKey;
    theme?: Theme;
    contrast?: 0 | 1 | 2;
    color?: string;
    language?: string;
}

export enum Theme {
    Dark = 'dark',
    Light = 'light',
    System = 'system',
}
