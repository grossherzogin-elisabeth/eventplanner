import { describe, expect, it } from 'vitest';
import { useEventAdministrationUseCase } from '@/application/usecases';

describe('EventAdministrationUseCase', () => {
    it('should construct instance with useEventAdministrationUseCase', () => {
        const testee = useEventAdministrationUseCase();
        expect(testee).toBeDefined();
    });
});
