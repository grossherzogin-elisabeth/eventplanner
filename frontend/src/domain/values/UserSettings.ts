import type { PositionKey } from '@/domain';

export interface UserSettings {
    preferredPosition?: PositionKey;
    theme?: Theme;
    color?: string;
}

export enum Theme {
    Dark = 'dark',
    Light = 'light',
    System = 'system',
}
