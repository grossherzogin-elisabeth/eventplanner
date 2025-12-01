import type { ErrorHandlingService, QualificationCachingService } from '@/application/services';
import type { Qualification } from '@/domain';

export class QualificationUseCase {
    private readonly qualificationCachingService: QualificationCachingService;
    private readonly errorHandlingService: ErrorHandlingService;

    constructor(params: { qualificationCachingService: QualificationCachingService; errorHandlingService: ErrorHandlingService }) {
        this.qualificationCachingService = params.qualificationCachingService;
        this.errorHandlingService = params.errorHandlingService;
    }

    public async getQualifications(): Promise<Qualification[]> {
        try {
            return await this.qualificationCachingService.getQualifications();
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }
}
