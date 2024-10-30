import type { SignedInUser } from '@/domain';

export interface AccountRepository {
    getAccount(): Promise<SignedInUser | undefined>;
    login(redirectTo?: string): Promise<void>;
    logout(): Promise<void>;
}
