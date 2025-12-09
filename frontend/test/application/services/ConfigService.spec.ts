import { describe, expect, it } from 'vitest';
import { useConfigService } from '@/application/services';

describe('ConfigService', () => {
    it('should construct instance with useConfigService', () => {
        const testee = useConfigService();
        expect(testee).toBeDefined();
    });
});
