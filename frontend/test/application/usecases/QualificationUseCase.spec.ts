import { useQualificationsUseCase } from '@/application/usecases';
import { describe, expect, it } from 'vitest';

describe('QualificationsUseCase', () => {
    it('should construct instance with useQualificationsUseCase', () => {
        const testee = useQualificationsUseCase();
        expect(testee).toBeDefined();
    });
});
