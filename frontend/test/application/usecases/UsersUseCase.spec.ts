import { describe, expect, it } from 'vitest';
import { useUsersUseCase } from '@/application/usecases';

describe('UserUseCase', () => {
    it('should construct instance with useUsersUseCase', () => {
        const testee = useUsersUseCase();
        expect(testee).toBeDefined();
    });
});
