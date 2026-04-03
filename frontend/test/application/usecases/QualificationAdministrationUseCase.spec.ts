import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { QualificationRepository } from '@/application/ports';
import type { ErrorHandlingService, NotificationService, QualificationCachingService } from '@/application/services';
import { QualificationAdministrationUseCase } from '@/application/usecases/QualificationAdministrationUseCase';
import type { QualificationService } from '@/domain';
import {
    mockQualificationCaptain,
    mockQualificationDeckhand,
    mockQualificationEngineer,
    mockQualificationExpires,
    mockQualificationGeneric,
    mockQualificationMate,
} from '~/mocks';
import { CAPTAIN } from '~/mocks/keys';

describe('QualificationAdministrationUseCase', () => {
    let testee: QualificationAdministrationUseCase;
    let qualificationCachingService: QualificationCachingService;
    let qualificationService: QualificationService;
    let qualificationRepository: QualificationRepository;
    let notificationService: NotificationService;
    let errorHandlingService: ErrorHandlingService;

    beforeEach(() => {
        qualificationCachingService = {
            getQualifications: vi.fn(async () => [
                mockQualificationCaptain(),
                mockQualificationEngineer(),
                mockQualificationMate(),
                mockQualificationDeckhand(),
                mockQualificationExpires(),
                mockQualificationGeneric(),
            ]),
            updateCache: vi.fn(async (qualification) => qualification),
            removeFromCache: vi.fn(async () => undefined),
        } as unknown as QualificationCachingService;
        qualificationService = {
            doesQualificationMatchFilter: vi.fn(() => true),
        } as unknown as QualificationService;
        qualificationRepository = {
            create: vi.fn(async (qualification) => qualification),
            update: vi.fn(async (_key, qualification) => qualification),
            deleteByKey: vi.fn(async () => undefined),
        } as unknown as QualificationRepository;
        notificationService = { success: vi.fn() } as unknown as NotificationService;
        errorHandlingService = { handleRawError: vi.fn() } as unknown as ErrorHandlingService;

        testee = new QualificationAdministrationUseCase({
            qualificationCachingService,
            qualificationService,
            qualificationRepository,
            notificationService,
            errorHandlingService,
        });
    });

    describe('getQualifications', () => {
        it('should return qualifications sorted alphabetically by name', async () => {
            const result = await testee.getQualifications();

            expect(result.map((q) => q.name)).toEqual(['Captain', 'Deckhand', 'Engineer', 'Expires', 'Generic', 'Mate']);
        });

        it('should pass filters to qualification service for each qualification', async () => {
            const filters = { text: 'cap', expires: true, grantsPosition: [CAPTAIN] };

            await testee.getQualifications(filters);

            expect(qualificationService.doesQualificationMatchFilter).toHaveBeenCalledTimes(6);
            expect(qualificationService.doesQualificationMatchFilter).toHaveBeenCalledWith(mockQualificationCaptain(), filters);
        });

        it('should exclude qualifications not matching the filter', async () => {
            qualificationService.doesQualificationMatchFilter = vi.fn((q) => q.expires);

            const result = await testee.getQualifications({ expires: true });

            expect(result.map((q) => q.key)).toEqual([mockQualificationCaptain().key, mockQualificationExpires().key]);
        });

        it('should return all qualifications when no filters are provided', async () => {
            const result = await testee.getQualifications();

            expect(qualificationService.doesQualificationMatchFilter).not.toHaveBeenCalled();
            expect(result).toHaveLength(6);
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

    describe('createQualification', () => {
        it('should save qualification, update cache and show success notification', async () => {
            const qualification = mockQualificationCaptain();
            qualificationRepository.create = vi.fn(async () => qualification);

            const result = await testee.createQualification(qualification);

            expect(qualificationRepository.create).toHaveBeenCalledWith(qualification);
            expect(qualificationCachingService.updateCache).toHaveBeenCalledWith(qualification);
            expect(notificationService.success).toHaveBeenCalled();
            expect(result).toEqual(qualification);
        });

        it('should handle errors and rethrow', async () => {
            const error = new Error('create failed');
            qualificationRepository.create = vi.fn(async () => {
                throw error;
            });

            try {
                await testee.createQualification(mockQualificationCaptain());
                expect(true).toBe(false); // fail test when promise was not rejected
            } catch (e) {
                expect(e).toBe(error);
                expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
                expect(notificationService.success).not.toHaveBeenCalled();
            }
        });
    });

    describe('updateQualification', () => {
        it('should update qualification, refresh cache and show success notification', async () => {
            const qualification = mockQualificationMate();
            qualificationRepository.update = vi.fn(async () => qualification);

            const result = await testee.updateQualification(qualification);

            expect(qualificationRepository.update).toHaveBeenCalledWith(qualification.key, qualification);
            expect(qualificationCachingService.updateCache).toHaveBeenCalledWith(qualification);
            expect(notificationService.success).toHaveBeenCalled();
            expect(result).toEqual(qualification);
        });

        it('should handle errors and rethrow', async () => {
            const error = new Error('update failed');
            qualificationRepository.update = vi.fn(async () => {
                throw error;
            });

            try {
                await testee.updateQualification(mockQualificationMate());
                expect(true).toBe(false); // fail test when promise was not rejected
            } catch (e) {
                expect(e).toBe(error);
                expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
                expect(notificationService.success).not.toHaveBeenCalled();
            }
        });
    });

    describe('deleteQualification', () => {
        it('should delete qualification from repository and remove from cache', async () => {
            const qualification = mockQualificationDeckhand();

            await testee.deleteQualification(qualification);

            expect(qualificationRepository.deleteByKey).toHaveBeenCalledWith(qualification.key);
            expect(qualificationCachingService.removeFromCache).toHaveBeenCalledWith(qualification.key);
            expect(notificationService.success).toHaveBeenCalled();
        });

        it('should handle errors and rethrow', async () => {
            const error = new Error('delete failed');
            qualificationRepository.deleteByKey = vi.fn(async () => {
                throw error;
            });

            try {
                await testee.deleteQualification(mockQualificationDeckhand());
                expect(true).toBe(false); // fail test when promise was not rejected
            } catch (e) {
                expect(e).toBe(error);
                expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
                expect(notificationService.success).not.toHaveBeenCalled();
            }
        });
    });
});
