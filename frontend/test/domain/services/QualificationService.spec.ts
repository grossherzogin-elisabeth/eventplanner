import { QualificationService, useQualificationService } from '@/domain';
import { beforeEach, describe, expect, it } from 'vitest';
import { mockQualificationCaptain } from '~/mocks';

const ERR_REQUIRED = 'generic.validation.required';

describe('QualificationService', () => {
    let testee: QualificationService;

    beforeEach(() => {
        testee = new QualificationService();
    });

    it('should construct instance with useQualificationService', () => {
        const testee = useQualificationService();
        expect(testee).toBeDefined();
    });

    describe('validate', () => {
        it('should return empty object if input is valid', () => {
            expect(testee.validate(mockQualificationCaptain())).toEqual({});
        });

        it('should return errors for missing required attributes', () => {
            expect(testee.validate(mockQualificationCaptain({ name: undefined }))).toEqual({ name: [ERR_REQUIRED] });
            expect(testee.validate(mockQualificationCaptain({ description: undefined }))).toEqual({ description: [ERR_REQUIRED] });
            expect(testee.validate(mockQualificationCaptain({ icon: undefined }))).toEqual({ icon: [ERR_REQUIRED] });
        });

        it('should return errors for missing required attributes', () => {
            const input = mockQualificationCaptain({
                name: undefined,
                description: undefined,
                icon: undefined,
            });
            expect(testee.validate(input)).toEqual({
                name: [ERR_REQUIRED],
                description: [ERR_REQUIRED],
                icon: [ERR_REQUIRED],
            });
        });
    });
});
