import { describe, expect, it } from 'vitest';
import { useErrorHandlingService } from '@/application/services';

describe('ErrorHandlingService', () => {
    it('should construct instance with useErrorHandlingService', () => {
        const testee = useErrorHandlingService();
        expect(testee).toBeDefined();
    });
});
