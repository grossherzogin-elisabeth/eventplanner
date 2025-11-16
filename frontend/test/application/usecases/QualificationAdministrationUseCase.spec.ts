import { useQualificationsAdministrationUseCase } from '@/application/usecases';
import { describe, expect, it } from 'vitest';

describe('QualificationsAdministrationUseCase', () => {
    it('should construct instance with useQualificationsAdministrationUseCase', () => {
        const testee = useQualificationsAdministrationUseCase();
        expect(testee).toBeDefined();
    });
});
