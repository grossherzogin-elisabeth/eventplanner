import { describe, expect, it } from 'vitest';
import { useUserAdministrationUseCase } from '@/application/usecases';

describe('UserAdministrationUseCase', () => {
    it('should construct instance with useUserAdministrationUseCase', () => {
        const testee = useUserAdministrationUseCase();
        expect(testee).toBeDefined();
    });
});
