import { beforeEach, describe, expect, it } from 'vitest';
import { PositionService, usePositionService } from '@/domain';
import { mockPositionCaptain, mockPositions } from '~/mocks';

const ERR_REQUIRED = 'generic.validation.required';
const ERR_DUPLICATE_KEY = 'views.settings.positions.validation.key-must-be-unique';
const ERR_DUPLICATE_NAME = 'views.settings.positions.validation.name-must-be-unique';

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

        it('should return error for duplicate keys and names', () => {
            const input = mockPositionCaptain();
            expect(testee.validate(input, mockPositions())).toEqual({
                key: [ERR_DUPLICATE_KEY],
                name: [ERR_DUPLICATE_NAME],
            });
        });
    });
});
