import { useUsersUseCase } from '@/application/usecases';
import { describe, expect, it } from 'vitest';

describe('UserUseCase', () => {
    it('should construct instance with useUsersUseCase', () => {
        const testee = useUsersUseCase();
        expect(testee).toBeDefined();
    });
});
