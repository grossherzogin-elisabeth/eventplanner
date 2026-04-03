import { type MockInstance, beforeEach, describe, expect, it, vi } from 'vitest';
import type { AuthService, ErrorHandlingService } from '@/application';
import type { EventRepository } from '@/application/ports';
import { EventExportUseCase } from '@/application/usecases/EventExportUseCase';
import * as DownloadUtils from '@/common/utils/DownloadUtils';
import { Permission } from '@/domain';
import { mockEvent } from '~/mocks';

describe('EventExportUseCase', () => {
    let testee: EventExportUseCase;
    let authService: AuthService;
    let errorHandlingService: ErrorHandlingService;
    let eventRepository: EventRepository;
    let saveBlobToFileSpy: MockInstance;

    beforeEach(() => {
        vi.clearAllMocks();
        saveBlobToFileSpy = vi.spyOn(DownloadUtils, 'saveBlobToFile').mockImplementation(() => undefined);
        authService = { hasPermission: vi.fn(() => true) } as unknown as AuthService;
        errorHandlingService = { handleRawError: vi.fn() } as unknown as ErrorHandlingService;
        eventRepository = {
            getExportTemplates: vi.fn(async () => ['default-template']),
            export: vi.fn(async () => new Blob(['xlsx'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })),
            exportEvent: vi.fn(async () => new Blob(['dummy'], { type: 'application/octet-stream' })),
        } as unknown as EventRepository;

        testee = new EventExportUseCase({ authService, errorHandlingService, eventRepository });
    });

    it('should fetch export templates when user has permission', async () => {
        const templates = await testee.getExportTemplates();

        expect(authService.hasPermission).toHaveBeenCalledWith(Permission.EXPORT_EVENTS);
        expect(eventRepository.getExportTemplates).toHaveBeenCalledOnce();
        expect(templates).toEqual(['default-template']);
    });

    it('should cache fetched export templates', async () => {
        await testee.getExportTemplates();
        const templates = await testee.getExportTemplates();

        expect(eventRepository.getExportTemplates).toHaveBeenCalledOnce();
        expect(templates).toEqual(['default-template']);
    });

    it('should return empty templates without export permission', async () => {
        authService.hasPermission = vi.fn(() => false);
        testee = new EventExportUseCase({ authService, errorHandlingService, eventRepository });

        const templates = await testee.getExportTemplates();

        expect(eventRepository.getExportTemplates).not.toHaveBeenCalled();
        expect(templates).toEqual([]);
    });

    it('should export event and save file with formatted date', async () => {
        const event = mockEvent({ start: new Date('2026-02-03T10:00:00Z') });
        const file = new Blob(['xlsx'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        eventRepository.exportEvent = vi.fn(async () => file);

        await testee.exportEvent(event, 'crew-plan');

        expect(eventRepository.exportEvent).toHaveBeenCalledWith(event, 'crew-plan');
        expect(saveBlobToFileSpy).toHaveBeenCalledWith('crew-plan_2026-02-03.xlsx', file);
        expect(errorHandlingService.handleRawError).not.toHaveBeenCalled();
    });

    it('should delegate export errors to error handling service', async () => {
        const event = mockEvent({ start: new Date('2026-02-03T10:00:00Z') });
        const error = new Error('export failed');
        eventRepository.exportEvent = vi.fn(async () => {
            throw error;
        });

        await testee.exportEvent(event, 'crew-plan');

        expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
    });

    it('should export events by year and save xlsx file', async () => {
        const file = new Blob(['xlsx'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        eventRepository.export = vi.fn(async () => file);

        await testee.exportEvents(2026);

        expect(eventRepository.export).toHaveBeenCalledWith(2026);
        expect(saveBlobToFileSpy).toHaveBeenCalledWith('Einsatzmatrix 2026.xlsx', file);
    });

    it('should handle year export errors and rethrow', async () => {
        const error = new Error('year export failed');
        eventRepository.export = vi.fn(async () => {
            throw error;
        });

        try {
            await testee.exportEvents(2026);
            expect(true).toBe(false); // fail test when promise was not rejected
        } catch (e) {
            expect(e).toBe(error);
            expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
        }
    });
});
