import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { UserRepository } from '@/application/ports';
import type {
    AuthService,
    ConfigService,
    ErrorHandlingService,
    NotificationService,
    PositionCachingService,
    UserCachingService,
} from '@/application/services';
import { defaultConfig } from '@/application/services/ConfigService.ts';
import { UsersUseCase } from '@/application/usecases/UsersUseCase';
import { Permission, Theme } from '@/domain';
import type { RegistrationService } from '@/domain/services/RegistrationService';
import {
    mockPositionCaptain,
    mockPositionDeckhand,
    mockSignedInUser,
    mockUserCaptain,
    mockUserDeckhand,
    mockUserDetails,
    mockUserEngineer,
} from '~/mocks';
import { CAPTAIN, MATE } from '~/mocks/keys';

describe('UsersUseCase', () => {
    let testee: UsersUseCase;
    let userRepository: UserRepository;
    let authService: AuthService;
    let configService: ConfigService;
    let positionCachingService: PositionCachingService;
    let userCachingService: UserCachingService;
    let notificationService: NotificationService;
    let errorHandlingService: ErrorHandlingService;

    beforeEach(() => {
        vi.stubGlobal(
            'matchMedia',
            vi.fn(() => ({ matches: false, addEventListener: vi.fn(), removeEventListener: vi.fn() }))
        );

        userRepository = {
            findByKey: vi.fn(async () => mockUserDetails(mockUserCaptain())),
            findBySignedInUser: vi.fn(async () => mockUserDetails(mockUserCaptain())),
            updateSignedInUser: vi.fn(async () => mockUserDetails(mockUserCaptain())),
        } as unknown as UserRepository;
        authService = {
            getSignedInUser: vi.fn(() => mockSignedInUser()),
        } as unknown as AuthService;
        configService = {
            getConfig: vi.fn(() => ({ overrideSignedInUserKey: undefined })),
        } as unknown as ConfigService;
        positionCachingService = {
            getPositions: vi.fn(async () => [mockPositionCaptain(), mockPositionDeckhand()]),
        } as unknown as PositionCachingService;
        userCachingService = {
            getUsers: vi.fn(async () => [mockUserCaptain(), mockUserDeckhand(), mockUserEngineer()]),
            updateCache: vi.fn(async (user) => user),
        } as unknown as UserCachingService;
        notificationService = { success: vi.fn() } as unknown as NotificationService;
        errorHandlingService = { handleRawError: vi.fn() } as unknown as ErrorHandlingService;

        testee = new UsersUseCase({
            userRepository,
            authService,
            configService,
            positionCachingService,
            userCachingService,
            notificationService,
            errorHandlingService,
            registrationService: {} as RegistrationService,
        });
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    describe('getUserDetailsForSignedInUser', () => {
        it('should fetch signed-in user details via findBySignedInUser when no override key is configured', async () => {
            const details = mockUserDetails(mockUserCaptain());
            userRepository.findBySignedInUser = vi.fn(async () => details);

            const result = await testee.getUserDetailsForSignedInUser();

            expect(userRepository.findBySignedInUser).toHaveBeenCalledOnce();
            expect(userRepository.findByKey).not.toHaveBeenCalled();
            expect(result.key).toBe(details.key);
        });

        it('should fetch by key when override key is configured and user has READ_USER_DETAILS permission', async () => {
            configService.getConfig = vi.fn(() => ({ ...defaultConfig, overrideSignedInUserKey: 'captain-key' }));
            authService.getSignedInUser = vi.fn(() => mockSignedInUser({ permissions: [Permission.READ_USER_DETAILS] }));

            await testee.getUserDetailsForSignedInUser();

            expect(userRepository.findByKey).toHaveBeenCalledWith('captain-key');
            expect(userRepository.findBySignedInUser).not.toHaveBeenCalled();
        });

        it('should sort position keys with preferred position first', async () => {
            localStorage.setItem('settings', JSON.stringify({ preferredPosition: MATE }));
            const details = mockUserDetails(mockUserCaptain()); // positionKeys: [CAPTAIN, MATE]
            userRepository.findBySignedInUser = vi.fn(async () => details);

            const result = await testee.getUserDetailsForSignedInUser();

            expect(result.positionKeys[0]).toBe(MATE);
        });

        it('should handle errors and rethrow', async () => {
            const error = new Error('fetch failed');
            userRepository.findBySignedInUser = vi.fn(async () => {
                throw error;
            });

            try {
                await testee.getUserDetailsForSignedInUser();
                expect(true).toBe(false); // fail test when promise was not rejected
            } catch (e) {
                expect(e).toBe(error);
                expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
            }
        });
    });

    describe('updateUserDetailsForSignedInUser', () => {
        it('should update signed-in user with changed fields, refresh cache and show success', async () => {
            const original = mockUserDetails(mockUserCaptain());
            const updated = { ...original, nickName: 'Charlie the Captain' };
            const saved = { ...original, nickName: 'Charlie the Captain' };
            userRepository.updateSignedInUser = vi.fn(async () => saved);

            const result = await testee.updateUserDetailsForSignedInUser(original, updated);

            expect(userRepository.updateSignedInUser).toHaveBeenCalledWith(expect.objectContaining({ nickName: 'Charlie the Captain' }));
            expect(userCachingService.updateCache).toHaveBeenCalled();
            expect(notificationService.success).toHaveBeenCalled();
            expect(result).toEqual(saved);
        });

        it('should skip repository call and return updated when there are no changes', async () => {
            const user = mockUserDetails(mockUserCaptain());

            const result = await testee.updateUserDetailsForSignedInUser(user, user);

            expect(userRepository.updateSignedInUser).not.toHaveBeenCalled();
            expect(notificationService.success).not.toHaveBeenCalled();
            expect(result).toEqual(user);
        });

        it('should handle errors and rethrow', async () => {
            const error = new Error('update failed');
            userRepository.updateSignedInUser = vi.fn(async () => {
                throw error;
            });
            const original = mockUserDetails(mockUserCaptain());
            const updated = { ...original, nickName: 'New Nick' };

            try {
                await testee.updateUserDetailsForSignedInUser(original, updated);
                expect(true).toBe(false); // fail test when promise was not rejected
            } catch (e) {
                expect(e).toBe(error);
                expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
            }
        });
    });

    describe('getUsers', () => {
        it('should return all users sorted alphabetically by first then last name', async () => {
            userCachingService.getUsers = vi.fn(async () => [mockUserDeckhand(), mockUserCaptain(), mockUserEngineer()]);

            const result = await testee.getUsers();

            expect(result.map((u) => u.firstName)).toEqual(['Alice', 'Charlie', 'Dean']);
        });

        it('should filter users by provided keys', async () => {
            const result = await testee.getUsers([mockUserCaptain().key]);

            expect(result).toHaveLength(1);
            expect(result[0].key).toBe(mockUserCaptain().key);
        });
    });

    describe('resolvePositionNames', () => {
        it('should return a map of position key to position', async () => {
            positionCachingService.getPositions = vi.fn(async () => [mockPositionCaptain(), mockPositionDeckhand()]);

            const result = await testee.resolvePositionNames();

            expect(result.get(CAPTAIN)?.name).toBe('Captain');
            expect(result.size).toBe(2);
        });
    });

    describe('resolveUserNames', () => {
        it('should return a map of user key to formatted full name', async () => {
            userCachingService.getUsers = vi.fn(async () => [mockUserCaptain()]);

            const result = await testee.resolveUserNames([mockUserCaptain().key]);

            expect(result.get(mockUserCaptain().key)).toBe('Charlie Captain');
        });
    });

    describe('getUserSettings', () => {
        it('should return default system theme when no settings are stored', async () => {
            const result = await testee.getUserSettings();

            expect(result.theme).toBe(Theme.System);
        });

        it('should return stored settings from localStorage', async () => {
            localStorage.setItem('settings', JSON.stringify({ theme: Theme.Dark, preferredPosition: CAPTAIN }));

            const result = await testee.getUserSettings();

            expect(result.theme).toBe(Theme.Dark);
            expect(result.preferredPosition).toBe(CAPTAIN);
        });
    });

    describe('saveUserSettings', () => {
        it('should persist merged settings to localStorage', async () => {
            await testee.saveUserSettings({ theme: Theme.Dark });

            const stored = JSON.parse(localStorage.getItem('settings') ?? '{}');
            expect(stored.theme).toBe(Theme.Dark);
        });

        it('should merge patch with existing settings', async () => {
            localStorage.setItem('settings', JSON.stringify({ preferredPosition: CAPTAIN }));

            const result = await testee.saveUserSettings({ theme: Theme.Light });

            expect(result.theme).toBe(Theme.Light);
            expect(result.preferredPosition).toBe(CAPTAIN);
        });
    });

    describe('applyUserSettings', () => {
        it('should add dark class to html element when theme is dark', async () => {
            await testee.applyUserSettings({ theme: Theme.Dark });

            expect(document.querySelector('html')?.classList.contains('dark')).toBe(true);
        });

        it('should remove dark class from html element when theme is light', async () => {
            document.querySelector('html')?.classList.add('dark');

            await testee.applyUserSettings({ theme: Theme.Light });

            expect(document.querySelector('html')?.classList.contains('dark')).toBe(false);
        });

        it('should apply dark class when theme is system and system prefers dark', async () => {
            vi.stubGlobal(
                'matchMedia',
                vi.fn(() => ({ matches: true, addEventListener: vi.fn(), removeEventListener: vi.fn() }))
            );

            await testee.applyUserSettings({ theme: Theme.System });

            expect(document.querySelector('html')?.classList.contains('dark')).toBe(true);
        });
    });
});
