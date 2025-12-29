import { describe, expect, it } from 'vitest';
import { usePositionCachingService } from '@/application/services';

describe('PositionCachingService', () => {
    it('should construct instance with usePositionCachingService', () => {
        const testee = usePositionCachingService();
        expect(testee).toBeDefined();
    });
});
