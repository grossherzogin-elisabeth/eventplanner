import { describe, expect, it } from 'vitest';
import { useEventUseCase } from '@/application/usecases';

describe('EventUseCase', () => {
    it('should construct instance with useEventUseCase', () => {
        const testee = useEventUseCase();
        expect(testee).toBeDefined();
    });
});
