import type { ErrorHandlingService, PositionCachingService } from '@/application/services';
import type { Position } from '@/domain';

export class PositionUseCase {
    private readonly positionCachingService: PositionCachingService;
    private readonly errorHandlingService: ErrorHandlingService;

    constructor(params: { positionCachingService: PositionCachingService; errorHandlingService: ErrorHandlingService }) {
        this.positionCachingService = params.positionCachingService;
        this.errorHandlingService = params.errorHandlingService;
    }

    public async getPositions(): Promise<Position[]> {
        try {
            return await this.positionCachingService.getPositions();
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }
}
