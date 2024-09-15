import { inject } from 'vue';
import type {
    AuthUseCase,
    EventAdministrationUseCase,
    EventUseCase,
    NotificationService,
    UserAdministrationUseCase,
    UsersUseCase,
} from '@/application';
import type { ErrorHandlingService } from '@/application/services/ErrorHandlingService';

export const AUTH_USE_CASE = 'application.usecase.auth';
export const EVENT_USE_CASE = 'application.usecase.events';
export const USER_USE_CASE = 'application.usecase.users';
export const USER_ADMIN_USE_CASE = 'application.usecase.useradmin';
export const EVENT_ADMIN_USE_CASE = 'application.usecase.eventadmin';
export const ERROR_HANDLING_SERVICE = 'application.usecase.errorhandling';
export const NOTIFICATION_SERVICE = 'application.service.notifications';

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
