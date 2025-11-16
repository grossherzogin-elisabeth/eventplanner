import { useEventCachingService } from '@/application/services';
import { describe, expect, it } from 'vitest';

describe('EventCachingService', () => {
    it('should construct instance with useEventCachingService', () => {
        const testee = useEventCachingService();
        expect(testee).toBeDefined();
    });
});
