import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { AccountRepository, UserRepository } from '@/application/ports';
import type { AuthService, ConfigService, NotificationService } from '@/application/services';
import { defaultConfig } from '@/application/services/ConfigService.ts';
import { AuthUseCase } from '@/application/usecases/AuthUseCase';
import { Permission } from '@/domain';
import { mockSignedInUser, mockUserCaptain, mockUserDetails } from '~/mocks';

describe('AuthUseCase', () => {
    let testee: AuthUseCase;
    let accountRepository: AccountRepository;
    let userRepository: UserRepository;
    let authService: AuthService;
    let configService: ConfigService;
    let notificationService: NotificationService;

    beforeEach(() => {
        accountRepository = {
            getAccount: vi.fn(async () => mockSignedInUser()),
            login: vi.fn(async () => undefined),
            logout: vi.fn(async () => undefined),
        } as unknown as AccountRepository;
        userRepository = {
            findByKey: vi.fn(async () => mockUserDetails(mockUserCaptain())),
        } as unknown as UserRepository;
        authService = {
            getSignedInUser: vi.fn(() => undefined),
            setSignedInUser: vi.fn(),
            setOffline: vi.fn(),
            impersonate: vi.fn(),
            onChange: vi.fn(() => (): void => undefined),
            onLogin: vi.fn(() => (): void => undefined),
            onLogout: vi.fn(() => (): void => undefined),
        } as unknown as AuthService;
        configService = {
            getConfig: vi.fn(() => ({ ...defaultConfig, overrideSignedInUserKey: undefined })),
        } as unknown as ConfigService;
        notificationService = {
            warning: vi.fn(),
        } as unknown as NotificationService;

        testee = new AuthUseCase({
            accountRepository,
            userRepository,
            authService,
            configService,
            notificationService,
        });
    });

    it('should authenticate with cached user and trigger only one background refresh', async () => {
        const signedInUser = mockSignedInUser();
        authService.getSignedInUser = vi.fn(() => signedInUser);

        const authenticated1 = await testee.authenticate();
        const authenticated2 = await testee.authenticate();

        expect(authenticated1).toEqual(signedInUser);
        expect(authenticated2).toEqual(signedInUser);
        expect(accountRepository.getAccount).toHaveBeenCalledOnce();
    });

    it('should trigger login redirect on background refresh failure when loginOnFailure is true', async () => {
        const signedInUser = mockSignedInUser();
        authService.getSignedInUser = vi.fn(() => signedInUser);
        accountRepository.getAccount = vi.fn(async () => {
            throw { status: 401 };
        });

        await testee.authenticate(true);

        await vi.waitFor(() => {
            expect(accountRepository.login).toHaveBeenCalledWith(location.href);
        });
    });

    it('should fetch account and store signed in user when no cached session exists', async () => {
        const signedInUser = mockSignedInUser({ firstName: 'Nina' });
        accountRepository.getAccount = vi.fn(async () => signedInUser);

        const authenticated = await testee.authenticate();

        expect(authenticated).toEqual(signedInUser);
        expect(accountRepository.getAccount).toHaveBeenCalledOnce();
        expect(authService.setSignedInUser).toHaveBeenCalledWith(signedInUser);
    });

    it('should enable offline mode and fail authentication when account fetch fails with network error', async () => {
        const error = new Error('Failed to fetch');
        accountRepository.getAccount = vi.fn(async () => {
            throw error;
        });

        try {
            await testee.authenticate();
            expect(true).toBe(false); // fail test when promise was not rejected
        } catch (e) {
            expect(e).toEqual(new Error('Unable to authenticate user'));
        }
        expect(authService.setOffline).toHaveBeenCalledOnce();
        expect(notificationService.warning).toHaveBeenCalled();
    });

    it('should clear signed in user on unauthorized fetch errors', async () => {
        accountRepository.getAccount = vi.fn(async () => {
            throw { status: 401 };
        });

        try {
            await testee.authenticate();
            expect(true).toBe(false); // fail test when promise was not rejected
        } catch (e) {
            expect(e).toEqual({ status: 401 });
        }
        expect(authService.setSignedInUser).toHaveBeenCalledWith(undefined);
        expect(authService.setOffline).not.toHaveBeenCalled();
    });

    it('should impersonate configured override user when caller has permission', async () => {
        const signedInUser = mockSignedInUser({ permissions: [Permission.READ_USER_DETAILS] });
        configService.getConfig = vi.fn(() => ({ ...defaultConfig, overrideSignedInUserKey: 'captain-key' }));
        accountRepository.getAccount = vi.fn(async () => signedInUser);

        await testee.authenticate();

        expect(userRepository.findByKey).toHaveBeenCalledWith('captain-key');
        expect(authService.impersonate).toHaveBeenCalledOnce();
    });

    it('should not impersonate without READ_USER_DETAILS permission', async () => {
        const signedInUser = mockSignedInUser({ permissions: [Permission.READ_EVENTS] });
        configService.getConfig = vi.fn(() => ({ ...defaultConfig, overrideSignedInUserKey: 'captain-key' }));
        accountRepository.getAccount = vi.fn(async () => signedInUser);

        await testee.authenticate();

        expect(userRepository.findByKey).not.toHaveBeenCalled();
        expect(authService.impersonate).not.toHaveBeenCalled();
    });

    it('should delegate login to account repository', async () => {
        await testee.login();
        expect(accountRepository.login).toHaveBeenCalledOnce();
    });

    it('should delegate logout to account repository', async () => {
        await testee.logout();
        expect(accountRepository.logout).toHaveBeenCalledOnce();
    });

    it('should persist override key and reload when impersonating a user', () => {
        const reload = vi.spyOn(globalThis.location, 'reload').mockImplementation((): void => undefined);

        testee.impersonateUser('u-123');

        expect(localStorage.getItem('eventplanner.overrideSignedInUserKey')).toBe('u-123');
        expect(reload).toHaveBeenCalledOnce();
    });

    it('should clear override key and reload when stopping impersonation', () => {
        localStorage.setItem('eventplanner.overrideSignedInUserKey', 'u-123');
        const reload = vi.spyOn(globalThis.location, 'reload').mockImplementation((): void => undefined);

        testee.impersonateUser(null);

        expect(localStorage.getItem('eventplanner.overrideSignedInUserKey')).toBe(null);
        expect(reload).toHaveBeenCalledOnce();
    });

    it('should resolve onAuthenticationDone with true when already authenticated', async () => {
        const signedInUser = mockSignedInUser();
        authService.getSignedInUser = vi.fn(() => signedInUser);

        const authenticated = await testee.onAuthenticationDone();
        expect(authenticated).toBe(true);
    });

    it('should resolve onAuthenticationDone with false after auth change event', async () => {
        authService.getSignedInUser = vi.fn(() => undefined);
        let listener: (() => void) | undefined;
        authService.onChange = vi.fn((cb: () => void) => {
            listener = cb;
            return (): void => undefined;
        }) as unknown as AuthService['onChange'];

        const done = testee.onAuthenticationDone();
        listener?.();

        expect(await done).toBe(false);
    });

    it('should resolve onLogin with current user', async () => {
        const signedInUser = mockSignedInUser();
        authService.getSignedInUser = vi.fn(() => signedInUser);
        expect(await testee.onLogin()).toEqual(signedInUser);
    });

    it('should resolve onLogin via login listener', async () => {
        const signedInUser = mockSignedInUser();
        authService.getSignedInUser = vi.fn(() => undefined);
        let loginListener: ((user: typeof signedInUser) => void) | undefined;
        authService.onLogin = vi.fn((cb: (user: typeof signedInUser) => void) => {
            loginListener = cb;
            return (): void => undefined;
        }) as unknown as AuthService['onLogin'];

        const onLogin = testee.onLogin();
        loginListener?.(signedInUser);
        expect(await onLogin).toEqual(signedInUser);
    });

    it('should resolve onLogout immediately when already signed out', async () => {
        authService.getSignedInUser = vi.fn(() => undefined);
        await testee.onLogout();
    });

    it('should resolve onLogout via logout listener', async () => {
        authService.getSignedInUser = vi.fn(() => mockSignedInUser());
        let logoutListener: (() => void) | undefined;
        authService.onLogout = vi.fn((cb: () => void) => {
            logoutListener = cb;
            return (): void => undefined;
        }) as unknown as AuthService['onLogout'];

        const onLogout = testee.onLogout();
        logoutListener?.();
        await onLogout;
    });
});
