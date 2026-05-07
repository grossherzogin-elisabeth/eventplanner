import type { EventRepository } from '@/application/ports';
import type { AuthService, ErrorHandlingService } from '@/application/services';
import { saveBlobToFile } from '@/common/utils/DownloadUtils';
import type { Event } from '@/domain';
import { Permission } from '@/domain';

export class EventExportUseCase {
    private readonly authService: AuthService;
    private readonly errorHandlingService: ErrorHandlingService;
    private readonly eventRepository: EventRepository;
    private exportTemplates: string[] | undefined = undefined;

    constructor(params: { authService: AuthService; errorHandlingService: ErrorHandlingService; eventRepository: EventRepository }) {
        this.errorHandlingService = params.errorHandlingService;
        this.authService = params.authService;
        this.eventRepository = params.eventRepository;
    }

    public async getExportTemplates(): Promise<string[]> {
        if (!this.exportTemplates) {
            if (this.authService.hasPermission(Permission.EXPORT_EVENTS)) {
                console.log('📡 Fetching event export templates');
                this.exportTemplates = await this.eventRepository.getExportTemplates();
            } else {
                this.exportTemplates = [];
            }
        }
        return this.exportTemplates;
    }

    public async exportEvent(event: Event, template: string): Promise<void> {
        try {
            const file = await this.eventRepository.exportEvent(event, template);
            saveBlobToFile(`${template}_${this.formatDate(event.start)}.xlsx`, file);
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
        }
    }

    public async exportEvents(year: number): Promise<void> {
        try {
            const blob = await this.eventRepository.export(year);
            saveBlobToFile(`Einsatzmatrix ${year}.xlsx`, blob);
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    private formatDate(date: Date | string | number): string {
        const d = new Date(date);
        const day = d.getDate() < 10 ? `0${d.getDate()}` : d.getDate().toString();
        const month = d.getMonth() < 9 ? `0${d.getMonth() + 1}` : (d.getMonth() + 1).toString();
        return `${d.getFullYear()}-${month}-${day}`;
    }
}
