import { useAppSettingsUseCase } from '@/application/usecases';
import { describe, expect, it } from 'vitest';

describe('AppSettingsUseCase', () => {
    it('should construct instance with useAppSettingsUseCase', () => {
        const testee = useAppSettingsUseCase();
        expect(testee).toBeDefined();
    });
});
