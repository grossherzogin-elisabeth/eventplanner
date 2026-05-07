import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { ErrorHandlingService, PositionCachingService } from '@/application/services';
import { PositionUseCase } from '@/application/usecases/PositionUseCase';
import { mockPositionCaptain, mockPositionDeckhand } from '~/mocks';

describe('PositionUseCase', () => {
    let testee: PositionUseCase;
    let positionCachingService: PositionCachingService;
    let errorHandlingService: ErrorHandlingService;

    beforeEach(() => {
        positionCachingService = {
            getPositions: vi.fn(async () => [mockPositionCaptain(), mockPositionDeckhand()]),
        } as unknown as PositionCachingService;
        errorHandlingService = { handleRawError: vi.fn() } as unknown as ErrorHandlingService;

        testee = new PositionUseCase({ positionCachingService, errorHandlingService });
    });

    it('should return positions from caching service', async () => {
        const result = await testee.getPositions();

        expect(positionCachingService.getPositions).toHaveBeenCalledOnce();
        expect(result).toEqual([mockPositionCaptain(), mockPositionDeckhand()]);
    });

    it('should handle errors and rethrow', async () => {
        const error = new Error('cache failed');
        positionCachingService.getPositions = vi.fn(async () => {
            throw error;
        });

        try {
            await testee.getPositions();
            expect(true).toBe(false); // fail test when promise was not rejected
        } catch (e) {
            expect(e).toBe(error);
            expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
        }
    });
});
