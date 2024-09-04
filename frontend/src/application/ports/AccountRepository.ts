import type { SignedInUser } from '@/domain';

export interface AccountRepository {
    getAccount(): Promise<SignedInUser | undefined>;
}
