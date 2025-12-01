import { useEventAdministrationUseCase } from '@/application/usecases';
import { describe, expect, it } from 'vitest';

describe('EventAdministrationUseCase', () => {
    it('should construct instance with useEventAdministrationUseCase', () => {
        const testee = useEventAdministrationUseCase();
        expect(testee).toBeDefined();
    });
});
