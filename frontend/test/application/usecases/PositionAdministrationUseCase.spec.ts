import { usePositionAdministrationUseCase } from '@/application/usecases';
import { describe, expect, it } from 'vitest';

describe('PositionAdministrationUseCase', () => {
    it('should construct instance with usePositionAdministrationUseCase', () => {
        const testee = usePositionAdministrationUseCase();
        expect(testee).toBeDefined();
    });
});
