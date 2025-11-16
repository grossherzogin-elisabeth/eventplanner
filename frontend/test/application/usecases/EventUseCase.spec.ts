import { useEventUseCase } from '@/application/usecases';
import { describe, expect, it } from 'vitest';

describe('EventUseCase', () => {
    it('should construct instance with useEventUseCase', () => {
        const testee = useEventUseCase();
        expect(testee).toBeDefined();
    });
});
