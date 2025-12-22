import { describe, expect, it } from 'vitest';
import { usePositionAdministrationUseCase } from '@/application/usecases';

describe('PositionAdministrationUseCase', () => {
    it('should construct instance with usePositionAdministrationUseCase', () => {
        const testee = usePositionAdministrationUseCase();
        expect(testee).toBeDefined();
    });
});
