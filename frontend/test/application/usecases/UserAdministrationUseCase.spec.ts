import { useUserAdministrationUseCase } from '@/application/usecases';
import { describe, expect, it } from 'vitest';

describe('UserAdministrationUseCase', () => {
    it('should construct instance with useUserAdministrationUseCase', () => {
        const testee = useUserAdministrationUseCase();
        expect(testee).toBeDefined();
    });
});
