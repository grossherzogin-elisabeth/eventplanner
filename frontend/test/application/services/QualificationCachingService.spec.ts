import { describe, expect, it } from 'vitest';
import { useQualificationCachingService } from '@/application/services';

describe('QualificationCachingService', () => {
    it('should construct instance with useQualificationCachingService', () => {
        const testee = useQualificationCachingService();
        expect(testee).toBeDefined();
    });
});
