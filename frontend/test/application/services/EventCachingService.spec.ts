import { describe, expect, it } from 'vitest';
import { useEventCachingService } from '@/application/services';

describe('EventCachingService', () => {
    it('should construct instance with useEventCachingService', () => {
        const testee = useEventCachingService();
        expect(testee).toBeDefined();
    });
});
