import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { PositionRepository } from '@/application/ports';
import type { ErrorHandlingService, NotificationService, PositionCachingService } from '@/application/services';
import { PositionAdministrationUseCase } from '@/application/usecases/PositionAdministrationUseCase';
import { mockPositionCaptain, mockPositionDeckhand, mockPositionEngineer, mockPositionMate } from '~/mocks';

describe('PositionAdministrationUseCase', () => {
    let testee: PositionAdministrationUseCase;
    let positionCachingService: PositionCachingService;
    let positionRepository: PositionRepository;
    let notificationService: NotificationService;
    let errorHandlingService: ErrorHandlingService;

    beforeEach(() => {
        positionCachingService = {
            getPositions: vi.fn(async () => [
                mockPositionCaptain({ prio: 0 }),
                mockPositionEngineer({ prio: 1 }),
                mockPositionMate({ prio: 2 }),
                mockPositionDeckhand({ prio: 3 }),
            ]),
            updateCache: vi.fn(async (position) => position),
            removeFromCache: vi.fn(async () => undefined),
        } as unknown as PositionCachingService;
        positionRepository = {
            create: vi.fn(async (position) => position),
            update: vi.fn(async (_key, position) => position),
            deleteByKey: vi.fn(async () => undefined),
        } as unknown as PositionRepository;
        notificationService = { success: vi.fn() } as unknown as NotificationService;
        errorHandlingService = { handleRawError: vi.fn() } as unknown as ErrorHandlingService;

        testee = new PositionAdministrationUseCase({
            positionCachingService,
            positionRepository,
            notificationService,
            errorHandlingService,
        });
    });

    describe('getPositions', () => {
        it('should return positions sorted by priority descending', async () => {
            const result = await testee.getPositions();

            expect(result.map((p) => p.key)).toEqual([
                mockPositionDeckhand().key,
                mockPositionMate().key,
                mockPositionEngineer().key,
                mockPositionCaptain().key,
            ]);
        });

        it('should sort positions with equal priority alphabetically by name', async () => {
            positionCachingService.getPositions = vi.fn(async () => [
                mockPositionMate({ name: 'Zebra', prio: 1 }),
                mockPositionCaptain({ name: 'Alpha', prio: 1 }),
            ]);

            const result = await testee.getPositions();

            expect(result.map((p) => p.name)).toEqual(['Alpha', 'Zebra']);
        });

        it('should filter positions by name case-insensitively', async () => {
            const result = await testee.getPositions('captain');

            expect(result).toHaveLength(1);
            expect(result[0].key).toBe(mockPositionCaptain().key);
        });

        it('should ignore whitespace-only filter and return all positions', async () => {
            const result = await testee.getPositions('   ');

            expect(result).toHaveLength(4);
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

    describe('createPosition', () => {
        it('should save position, update cache and show success notification', async () => {
            const position = mockPositionCaptain();
            positionRepository.create = vi.fn(async () => position);

            const result = await testee.createPosition(position);

            expect(positionRepository.create).toHaveBeenCalledWith(position);
            expect(positionCachingService.updateCache).toHaveBeenCalledWith(position);
            expect(notificationService.success).toHaveBeenCalled();
            expect(result).toEqual(position);
        });

        it('should handle errors and rethrow', async () => {
            const error = new Error('create failed');
            positionRepository.create = vi.fn(async () => {
                throw error;
            });

            try {
                await testee.createPosition(mockPositionCaptain());
                expect(true).toBe(false); // fail test when promise was not rejected
            } catch (e) {
                expect(e).toBe(error);
                expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
                expect(notificationService.success).not.toHaveBeenCalled();
            }
        });
    });

    describe('updatePosition', () => {
        it('should update position, refresh cache and show success notification', async () => {
            const position = mockPositionMate();
            positionRepository.update = vi.fn(async () => position);

            const result = await testee.updatePosition(position);

            expect(positionRepository.update).toHaveBeenCalledWith(position.key, position);
            expect(positionCachingService.updateCache).toHaveBeenCalledWith(position);
            expect(notificationService.success).toHaveBeenCalled();
            expect(result).toEqual(position);
        });

        it('should handle errors and rethrow', async () => {
            const error = new Error('update failed');
            positionRepository.update = vi.fn(async () => {
                throw error;
            });

            try {
                await testee.updatePosition(mockPositionMate());
                expect(true).toBe(false); // fail test when promise was not rejected
            } catch (e) {
                expect(e).toBe(error);
                expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
                expect(notificationService.success).not.toHaveBeenCalled();
            }
        });
    });

    describe('deletePosition', () => {
        it('should delete position from repository and remove from cache', async () => {
            const position = mockPositionDeckhand();

            await testee.deletePosition(position);

            expect(positionRepository.deleteByKey).toHaveBeenCalledWith(position.key);
            expect(positionCachingService.removeFromCache).toHaveBeenCalledWith(position.key);
            expect(notificationService.success).toHaveBeenCalled();
        });

        it('should handle errors and rethrow', async () => {
            const error = new Error('delete failed');
            positionRepository.deleteByKey = vi.fn(async () => {
                throw error;
            });

            try {
                await testee.deletePosition(mockPositionDeckhand());
                expect(true).toBe(false); // fail test when promise was not rejected
            } catch (e) {
                expect(e).toBe(error);
                expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
                expect(notificationService.success).not.toHaveBeenCalled();
            }
        });
    });
});
