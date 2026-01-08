import type { SignedInUser } from '@/domain';

export interface AccountRepository {
    getAccount(): Promise<SignedInUser>;
    login(redirectTo?: string): Promise<void>;
    logout(): Promise<void>;
}
