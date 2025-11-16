import { useQualificationCachingService } from '@/application/services';
import { describe, expect, it } from 'vitest';

describe('QualificationCachingService', () => {
    it('should construct instance with useQualificationCachingService', () => {
        const testee = useQualificationCachingService();
        expect(testee).toBeDefined();
    });
});
