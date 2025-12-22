import { describe, expect, it } from 'vitest';
import { useQualificationsAdministrationUseCase } from '@/application/usecases';

describe('QualificationsAdministrationUseCase', () => {
    it('should construct instance with useQualificationsAdministrationUseCase', () => {
        const testee = useQualificationsAdministrationUseCase();
        expect(testee).toBeDefined();
    });
});
