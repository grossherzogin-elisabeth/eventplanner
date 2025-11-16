import { usePositionUseCase } from '@/application/usecases';
import { describe, expect, it } from 'vitest';

describe('PositionUseCase', () => {
    it('should construct instance with usePositionUseCase', () => {
        const testee = usePositionUseCase();
        expect(testee).toBeDefined();
    });
});
