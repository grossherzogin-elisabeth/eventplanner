import { describe, expect, it } from 'vitest';
import { usePositionUseCase } from '@/application/usecases';

describe('PositionUseCase', () => {
    it('should construct instance with usePositionUseCase', () => {
        const testee = usePositionUseCase();
        expect(testee).toBeDefined();
    });
});
