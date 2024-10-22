import type { PositionKey } from '@/domain';

export type QualificationKey = string;
export interface Qualification {
    key: QualificationKey;
    name: string;
    icon: string;
    description: string;
    expires: boolean;
    grantsPosition?: PositionKey;
}
