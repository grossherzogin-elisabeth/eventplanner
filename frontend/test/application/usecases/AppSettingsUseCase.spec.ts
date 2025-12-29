import { describe, expect, it } from 'vitest';
import { useAppSettingsUseCase } from '@/application/usecases';

describe('AppSettingsUseCase', () => {
    it('should construct instance with useAppSettingsUseCase', () => {
        const testee = useAppSettingsUseCase();
        expect(testee).toBeDefined();
    });
});
