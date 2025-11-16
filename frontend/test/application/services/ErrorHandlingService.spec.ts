import { useErrorHandlingService } from '@/application/services';
import { describe, expect, it } from 'vitest';

describe('ErrorHandlingService', () => {
    it('should construct instance with useErrorHandlingService', () => {
        const testee = useErrorHandlingService();
        expect(testee).toBeDefined();
    });
});
