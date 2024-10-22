import type {
    ErrorHandlingService,
    NotificationService,
    QualificationCachingService,
    QualificationRepository,
} from '@/application';
import type { Qualification } from '@/domain';

export class QualificationAdministrationUseCase {
    private readonly qualificationCachingService: QualificationCachingService;
    private readonly qualificationRepository: QualificationRepository;
    private readonly notificationService: NotificationService;
    private readonly errorHandlingService: ErrorHandlingService;

    constructor(params: {
        qualificationCachingService: QualificationCachingService;
        qualificationRepository: QualificationRepository;
        notificationService: NotificationService;
        errorHandlingService: ErrorHandlingService;
    }) {
        this.qualificationCachingService = params.qualificationCachingService;
        this.qualificationRepository = params.qualificationRepository;
        this.notificationService = params.notificationService;
        this.errorHandlingService = params.errorHandlingService;
    }

    public async getQualifications(filter?: string): Promise<Qualification[]> {
        try {
            let qualifications = await this.qualificationCachingService.getQualifications();
            const filterLc = filter?.trim().toLowerCase();
            if (filterLc) {
                qualifications = qualifications.filter(
                    (q) =>
                        q.key.toLowerCase().includes(filterLc) ||
                        q.icon.toLowerCase().includes(filterLc) ||
                        q.name.toLowerCase().includes(filterLc) ||
                        q.description.toLowerCase().includes(filterLc)
                );
            }
            qualifications = qualifications.sort((a, b) => a.name.localeCompare(b.name));
            return qualifications;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async createQualification(qualification: Qualification): Promise<Qualification> {
        try {
            const savedQualification = await this.qualificationRepository.create(qualification);
            await this.qualificationCachingService.updateCache(savedQualification);
            this.notificationService.success('Qualifikation wurde gespeichert');
            return savedQualification;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async updateQualification(qualification: Qualification): Promise<Qualification> {
        try {
            const savedQualification = await this.qualificationRepository.update(qualification.key, qualification);
            await this.qualificationCachingService.updateCache(savedQualification);
            this.notificationService.success('Qualifikation wurde aktualisiert');
            return savedQualification;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async deleteQualification(qualification: Qualification): Promise<void> {
        try {
            await this.qualificationRepository.deleteByKey(qualification.key);
            await this.qualificationCachingService.removeFromCache(qualification.key);
            this.notificationService.success('Qualifikation wurde gelöscht');
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }
}
