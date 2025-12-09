import { describe, expect, it } from 'vitest';
import { useUserCachingService } from '@/application/services';

describe('UserCachingService', () => {
    it('should construct instance with useUserCachingService', () => {
        const testee = useUserCachingService();
        expect(testee).toBeDefined();
    });
});
