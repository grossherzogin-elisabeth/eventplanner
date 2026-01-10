import { beforeEach, describe, expect, it } from 'vitest';
import { QualificationService, useQualificationService } from '@/domain';
import { mockQualificationCaptain, mockQualifications } from '~/mocks';

const ERR_REQUIRED = 'generic.validation.required';
const ERR_DUPLICATE_KEY = 'views.settings.qualifications.validation.key-must-be-unique';
const ERR_DUPLICATE_NAME = 'views.settings.qualifications.validation.name-must-be-unique';

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

        it('should return error for duplicate keys and names', () => {
            const input = mockQualificationCaptain();
            expect(testee.validate(input, mockQualifications())).toEqual({
                key: [ERR_DUPLICATE_KEY],
                name: [ERR_DUPLICATE_NAME],
            });
        });
    });

    describe('doesQualificationMatchFilter', () => {
        it('should return true if no filter is provided', () => {
            const filters = {};
            expect(testee.doesQualificationMatchFilter(mockQualificationCaptain(), filters)).toBe(true);
        });
        it('should return true if text filter matches part of qualification icon', () => {
            const filters = { text: 'anchor' };
            expect(testee.doesQualificationMatchFilter(mockQualificationCaptain({ icon: 'fa-anchor' }), filters)).toBe(true);
        });

        it('should return true if text filter matches part of qualification name', () => {
            const filters = { text: 'cap' };
            expect(testee.doesQualificationMatchFilter(mockQualificationCaptain({ name: 'Captain' }), filters)).toBe(true);
        });

        it('should return true if text filter matches part of qualification description', () => {
            const filters = { text: 'scription' };
            expect(testee.doesQualificationMatchFilter(mockQualificationCaptain({ description: 'description' }), filters)).toBe(true);
        });

        it('should return false if text filter does not match any field', () => {
            const filters = { text: 'nonexistent' };
            expect(testee.doesQualificationMatchFilter(mockQualificationCaptain(), filters)).toBe(false);
        });

        it('should return true if text filter is an empty string', () => {
            const filters = { text: '' };
            expect(testee.doesQualificationMatchFilter(mockQualificationCaptain(), filters)).toBe(true);
        });

        it('should return true if expires = true matches', () => {
            const filters = { expires: true };
            expect(testee.doesQualificationMatchFilter(mockQualificationCaptain({ expires: true }), filters)).toBe(true);
        });

        it('should return false if expires = true does not match', () => {
            const filters = { expires: true };
            expect(testee.doesQualificationMatchFilter(mockQualificationCaptain({ expires: false }), filters)).toBe(false);
        });

        it('should return true if any position matches', () => {
            const filters = { grantsPosition: ['captain', 'other-a'] };
            expect(
                testee.doesQualificationMatchFilter(mockQualificationCaptain({ grantsPositions: ['other-b', 'captain'] }), filters)
            ).toBe(true);
        });

        it('should return true if all filters match', () => {
            const filters = { text: 'cap', expires: true, grantsPosition: ['captain', 'other-a'] };
            expect(
                testee.doesQualificationMatchFilter(
                    mockQualificationCaptain({ name: 'Captain', expires: true, grantsPositions: ['other-b', 'captain'] }),
                    filters
                )
            ).toBe(true);
        });

        it('should return false if not all filters match', () => {
            const filters = { text: 'some-filter-string', expires: true, grantsPosition: ['captain', 'other-a'] };
            const qualificationWithMismatchingName = mockQualificationCaptain({
                name: 'other',
                expires: true,
                grantsPositions: ['other-b', 'captain'],
            });
            expect(testee.doesQualificationMatchFilter(qualificationWithMismatchingName, filters)).toBe(false);
            const qualificationWithMismatchingExpires = mockQualificationCaptain({
                name: 'some-filter-string',
                expires: false,
                grantsPositions: ['other-b', 'captain'],
            });
            expect(testee.doesQualificationMatchFilter(qualificationWithMismatchingExpires, filters)).toBe(false);
            const qualificationWithMismatchingPositions = mockQualificationCaptain({
                name: 'some-filter-string',
                expires: true,
                grantsPositions: ['other-b'],
            });
            expect(testee.doesQualificationMatchFilter(qualificationWithMismatchingPositions, filters)).toBe(false);
        });
    });
});
