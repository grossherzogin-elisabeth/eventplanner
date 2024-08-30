import type { Position, Registration, User } from '@/domain';

export interface ResolvedRegistration extends Registration {
    name: string;
    position: Position;
    user?: User;
}
