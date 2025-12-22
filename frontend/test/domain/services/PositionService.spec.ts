import { beforeEach, describe, expect, it } from 'vitest';
import { PositionService, usePositionService } from '@/domain';
import { mockPositionCaptain } from '~/mocks';

const ERR_REQUIRED = 'generic.validation.required';

describe('PositionService', () => {
    let testee: PositionService;

    beforeEach(() => {
        testee = new PositionService();
    });

    it('should construct instance with usePositionService', () => {
        const testee = usePositionService();
        expect(testee).toBeDefined();
    });

    describe('validate', () => {
        it('should return empty object if input is valid', () => {
            expect(testee.validate(mockPositionCaptain())).toEqual({});
        });

        it('should return errors for missing required attributes', () => {
            expect(testee.validate(mockPositionCaptain({ name: undefined }))).toEqual({ name: [ERR_REQUIRED] });
            expect(testee.validate(mockPositionCaptain({ color: undefined }))).toEqual({ color: [ERR_REQUIRED] });
            expect(testee.validate(mockPositionCaptain({ imoListRank: undefined }))).toEqual({ imoListRank: [ERR_REQUIRED] });
        });

        it('should return errors for missing required attributes', () => {
            const input = mockPositionCaptain({
                name: undefined,
                color: undefined,
                imoListRank: undefined,
            });
            expect(testee.validate(input)).toEqual({
                name: [ERR_REQUIRED],
                color: [ERR_REQUIRED],
                imoListRank: [ERR_REQUIRED],
            });
        });
    });
});
