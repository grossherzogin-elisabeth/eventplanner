import { usePositionCachingService } from '@/application/services';
import { describe, expect, it } from 'vitest';

describe('PositionCachingService', () => {
    it('should construct instance with usePositionCachingService', () => {
        const testee = usePositionCachingService();
        expect(testee).toBeDefined();
    });
});
