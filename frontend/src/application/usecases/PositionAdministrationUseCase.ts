import type {
    ErrorHandlingService,
    NotificationService,
    PositionCachingService,
    PositionRepository,
} from '@/application';
import type { Position } from '@/domain';

export class PositionAdministrationUseCase {
    private readonly positionCachingService: PositionCachingService;
    private readonly positionRepository: PositionRepository;
    private readonly notificationService: NotificationService;
    private readonly errorHandlingService: ErrorHandlingService;

    constructor(params: {
        positionCachingService: PositionCachingService;
        positionRepository: PositionRepository;
        notificationService: NotificationService;
        errorHandlingService: ErrorHandlingService;
    }) {
        this.positionCachingService = params.positionCachingService;
        this.positionRepository = params.positionRepository;
        this.notificationService = params.notificationService;
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

    public async createPosition(position: Position): Promise<Position> {
        try {
            const savedPosition = await this.positionRepository.create(position);
            await this.positionCachingService.updateCache(savedPosition);
            this.notificationService.success('Position wurde gespeichert');
            return savedPosition;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async updatePosition(position: Position): Promise<Position> {
        try {
            const savedPosition = await this.positionRepository.update(position.key, position);
            await this.positionCachingService.updateCache(savedPosition);
            this.notificationService.success('Position wurde aktualisiert');
            return savedPosition;
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }

    public async deletePosition(position: Position): Promise<void> {
        try {
            await this.positionRepository.deleteByKey(position.key);
            await this.positionCachingService.removeFromCache(position.key);
            this.notificationService.success('Position wurde gelöscht');
        } catch (e) {
            this.errorHandlingService.handleRawError(e);
            throw e;
        }
    }
}
