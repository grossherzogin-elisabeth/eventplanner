import { useUserCachingService } from '@/application/services';
import { describe, expect, it } from 'vitest';

describe('UserCachingService', () => {
    it('should construct instance with useUserCachingService', () => {
        const testee = useUserCachingService();
        expect(testee).toBeDefined();
    });
});
