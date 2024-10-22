import { inject } from 'vue';
import type {
    AuthUseCase,
    Config,
    EventAdministrationUseCase,
    EventUseCase,
    NotificationService,
    PositionAdministrationUseCase,
    PositionUseCase,
    QualificationAdministrationUseCase,
    QualificationUseCase,
    UserAdministrationUseCase,
    UsersUseCase,
} from '@/application';
import type { ErrorHandlingService } from '@/application/services/ErrorHandlingService';
import type { AppSettingsUseCase } from '@/application/usecases/AppSettingsUseCase';

export const CONFIG = 'application.config';
export const AUTH_USE_CASE = 'application.usecase.auth';
export const EVENT_USE_CASE = 'application.usecase.events';
export const USER_USE_CASE = 'application.usecase.users';
export const USER_ADMIN_USE_CASE = 'application.usecase.useradmin';
export const EVENT_ADMIN_USE_CASE = 'application.usecase.eventadmin';
export const APP_SETTINGS_USE_CASE = 'application.usecase.settings';
export const QUALIFICATION_USE_CASE = 'application.usecase.qualifications';
export const QUALIFICATION_ADMIN_USE_CASE = 'application.usecase.qualificationadmin';
export const POSITION_USE_CASE = 'application.usecase.positions';
export const POSITION_ADMIN_USE_CASE = 'application.usecase.positionadmin';
export const ERROR_HANDLING_SERVICE = 'application.usecase.errorhandling';
export const NOTIFICATION_SERVICE = 'application.service.notifications';

export function useConfig(): Config {
    const config = inject<Config>(CONFIG);
    if (!config) {
        throw new Error('Config not found!');
    }
    return config;
}

export function useAuthUseCase(): AuthUseCase {
    const useCase = inject<AuthUseCase>(AUTH_USE_CASE);
    if (!useCase) {
        throw new Error('Auth usecase not found!');
    }
    return useCase;
}

export function useEventUseCase(): EventUseCase {
    const useCase = inject<EventUseCase>(EVENT_USE_CASE);
    if (!useCase) {
        throw new Error('Event usecase not found!');
    }
    return useCase;
}

export function useEventAdministrationUseCase(): EventAdministrationUseCase {
    const useCase = inject<EventAdministrationUseCase>(EVENT_ADMIN_USE_CASE);
    if (!useCase) {
        throw new Error('Event admin usecase not found!');
    }
    return useCase;
}

export function useUsersUseCase(): UsersUseCase {
    const useCase = inject<UsersUseCase>(USER_USE_CASE);
    if (!useCase) {
        throw new Error('User usecase not found!');
    }
    return useCase;
}

export function useUserAdministrationUseCase(): UserAdministrationUseCase {
    const useCase = inject<UserAdministrationUseCase>(USER_ADMIN_USE_CASE);
    if (!useCase) {
        throw new Error('User admin usecase not found!');
    }
    return useCase;
}

export function useAppSettingsUseCase(): AppSettingsUseCase {
    const useCase = inject<AppSettingsUseCase>(APP_SETTINGS_USE_CASE);
    if (!useCase) {
        throw new Error('App settings usecase not found!');
    }
    return useCase;
}

export function useQualificationsUseCase(): QualificationUseCase {
    const useCase = inject<QualificationUseCase>(QUALIFICATION_USE_CASE);
    if (!useCase) {
        throw new Error('Qualification usecase not found!');
    }
    return useCase;
}

export function useQualificationsAdministrationUseCase(): QualificationAdministrationUseCase {
    const useCase = inject<QualificationAdministrationUseCase>(QUALIFICATION_ADMIN_USE_CASE);
    if (!useCase) {
        throw new Error('Qualification admin usecase not found!');
    }
    return useCase;
}

export function usePositionUseCase(): PositionUseCase {
    const useCase = inject<PositionUseCase>(POSITION_USE_CASE);
    if (!useCase) {
        throw new Error('Position usecase not found!');
    }
    return useCase;
}

export function usePositionAdministrationUseCase(): PositionAdministrationUseCase {
    const useCase = inject<PositionAdministrationUseCase>(POSITION_ADMIN_USE_CASE);
    if (!useCase) {
        throw new Error('Position admin usecase not found!');
    }
    return useCase;
}

export function useErrorHandling(): ErrorHandlingService {
    const service = inject<ErrorHandlingService>(ERROR_HANDLING_SERVICE);
    if (!service) {
        throw new Error('Error handling service not found!');
    }
    return service;
}

export function useNotifications(): NotificationService {
    const service = inject<NotificationService>(NOTIFICATION_SERVICE);
    if (!service) {
        throw new Error('Notification service not found!');
    }
    return service;
}
