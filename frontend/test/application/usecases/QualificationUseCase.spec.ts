import { describe, expect, it } from 'vitest';
import { useQualificationsUseCase } from '@/application/usecases';

describe('QualificationsUseCase', () => {
    it('should construct instance with useQualificationsUseCase', () => {
        const testee = useQualificationsUseCase();
        expect(testee).toBeDefined();
    });
});
