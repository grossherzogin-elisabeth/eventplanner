import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { ErrorHandlingService, QualificationCachingService } from '@/application/services';
import { QualificationUseCase } from '@/application/usecases';
import { mockQualificationCaptain, mockQualificationDeckhand } from '~/mocks';

describe('QualificationUseCase', () => {
    let testee: QualificationUseCase;
    let qualificationCachingService: QualificationCachingService;
    let errorHandlingService: ErrorHandlingService;

    beforeEach(() => {
        qualificationCachingService = {
            getQualifications: vi.fn(async () => [mockQualificationCaptain(), mockQualificationDeckhand()]),
        } as unknown as QualificationCachingService;
        errorHandlingService = { handleRawError: vi.fn() } as unknown as ErrorHandlingService;

        testee = new QualificationUseCase({ qualificationCachingService, errorHandlingService });
    });

    it('should return qualifications from caching service', async () => {
        const result = await testee.getQualifications();

        expect(qualificationCachingService.getQualifications).toHaveBeenCalledOnce();
        expect(result).toEqual([mockQualificationCaptain(), mockQualificationDeckhand()]);
    });

    it('should handle errors and rethrow', async () => {
        const error = new Error('cache failed');
        qualificationCachingService.getQualifications = vi.fn(async () => {
            throw error;
        });

        try {
            await testee.getQualifications();
            expect(true).toBe(false); // fail test when promise was not rejected
        } catch (e) {
            expect(e).toBe(error);
            expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
        }
    });
});
