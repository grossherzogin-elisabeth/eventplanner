import { useConfigService } from '@/application/services';
import { describe, expect, it } from 'vitest';

describe('ConfigService', () => {
    it('should construct instance with useConfigService', () => {
        const testee = useConfigService();
        expect(testee).toBeDefined();
    });
});
