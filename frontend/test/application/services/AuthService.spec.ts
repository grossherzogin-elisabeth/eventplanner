import { describe, expect, it } from 'vitest';
import { useAuthService } from '@/application/services';

describe('AuthService', () => {
    it('should construct instance with useAuthService', () => {
        const testee = useAuthService();
        expect(testee).toBeDefined();
    });
});
