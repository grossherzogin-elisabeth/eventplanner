import { useAuthService } from '@/application/services';
import { describe, expect, it } from 'vitest';

describe('AuthService', () => {
    it('should construct instance with useAuthService', () => {
        const testee = useAuthService();
        expect(testee).toBeDefined();
    });
});
