import { useAuthUseCase } from '@/application/usecases';
import { describe, expect, it } from 'vitest';

describe('AuthUseCase', () => {
    it('should construct instance with useAuthUseCase', () => {
        const testee = useAuthUseCase();
        expect(testee).toBeDefined();
    });
});
