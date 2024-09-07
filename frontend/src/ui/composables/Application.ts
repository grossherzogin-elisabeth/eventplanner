import { inject } from 'vue';
import type {
    AuthUseCase,
    EventAdministrationUseCase,
    EventUseCase,
    UserAdministrationUseCase,
    UsersUseCase,
} from '@/application';
import type { ErrorHandlingUseCase } from '@/application/usecases/ErrorHandlingUseCase';

export const AUTH_USE_CASE = 'application.usecase.auth';
export const EVENT_USE_CASE = 'application.usecase.events';
export const USER_USE_CASE = 'application.usecase.users';
export const USER_ADMIN_USE_CASE = 'application.usecase.useradmin';
export const EVENT_ADMIN_USE_CASE = 'application.usecase.eventadmin';
export const ERROR_HANDLING_USE_CASE = 'application.usecase.errorhandling';

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

export function useErrorHandling(): ErrorHandlingUseCase {
    const useCase = inject<ErrorHandlingUseCase>(ERROR_HANDLING_USE_CASE);
    if (!useCase) {
        throw new Error('Error handling usecase not found!');
    }
    return useCase;
}
