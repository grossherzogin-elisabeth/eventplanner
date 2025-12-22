import { describe, expect, it } from 'vitest';
import { useAuthUseCase } from '@/application/usecases';

describe('AuthUseCase', () => {
    it('should construct instance with useAuthUseCase', () => {
        const testee = useAuthUseCase();
        expect(testee).toBeDefined();
    });
});
