import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { UserRepository } from '@/application/ports';
import type { ErrorHandlingService, NotificationService, UserCachingService } from '@/application/services';
import { UserAdministrationUseCase } from '@/application/usecases/UserAdministrationUseCase';
import { mockUserCaptain, mockUserDeckhand, mockUserDetails } from '~/mocks';

describe('UserAdministrationUseCase', () => {
    let testee: UserAdministrationUseCase;
    let userRepository: UserRepository;
    let userCachingService: UserCachingService;
    let notificationService: NotificationService;
    let errorHandlingService: ErrorHandlingService;

    beforeEach(() => {
        userRepository = {
            createUser: vi.fn(async (user) => mockUserDetails(user)),
            updateUser: vi.fn(async () => mockUserDetails(mockUserCaptain())),
            deleteUser: vi.fn(async () => undefined),
            findByKey: vi.fn(async () => mockUserDetails(mockUserCaptain())),
        } as unknown as UserRepository;
        userCachingService = {
            updateCache: vi.fn(async (user) => user),
            removeFromCache: vi.fn(async () => undefined),
        } as unknown as UserCachingService;
        notificationService = { success: vi.fn() } as unknown as NotificationService;
        errorHandlingService = {
            handleRawError: vi.fn(),
            handleError: vi.fn(),
        } as unknown as ErrorHandlingService;

        testee = new UserAdministrationUseCase({
            userRepository,
            userCachingService,
            notificationService,
            errorHandlingService,
        });
    });

    describe('createUser', () => {
        it('should save user, update cache with projected fields and show success', async () => {
            const user = mockUserCaptain();
            const saved = mockUserDetails(user);
            userRepository.createUser = vi.fn(async () => saved);

            const result = await testee.createUser(user);

            expect(userRepository.createUser).toHaveBeenCalledWith(user);
            expect(userCachingService.updateCache).toHaveBeenCalledWith(
                expect.objectContaining({ key: saved.key, firstName: saved.firstName, email: saved.email })
            );
            expect(notificationService.success).toHaveBeenCalled();
            expect(result).toEqual(saved);
        });

        it('should handle errors and rethrow', async () => {
            const error = new Error('create failed');
            userRepository.createUser = vi.fn(async () => {
                throw error;
            });

            try {
                await testee.createUser(mockUserCaptain());
                expect(true).toBe(false); // fail test when promise was not rejected
            } catch (e) {
                expect(e).toBe(error);
                expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
                expect(notificationService.success).not.toHaveBeenCalled();
            }
        });
    });

    describe('updateUser', () => {
        it('should update user with changed fields, refresh cache and show success', async () => {
            const original = mockUserDetails(mockUserCaptain());
            const updated = { ...original, firstName: 'New Name' };
            const saved = { ...original, firstName: 'New Name' };
            userRepository.updateUser = vi.fn(async () => saved);

            const result = await testee.updateUser(original, updated);

            expect(userRepository.updateUser).toHaveBeenCalledWith(updated.key, expect.objectContaining({ firstName: 'New Name' }));
            expect(userCachingService.updateCache).toHaveBeenCalled();
            expect(notificationService.success).toHaveBeenCalled();
            expect(result).toEqual(saved);
        });

        it('should skip repository call and return updated when there are no changes', async () => {
            const user = mockUserDetails(mockUserCaptain());

            const result = await testee.updateUser(user, user);

            expect(userRepository.updateUser).not.toHaveBeenCalled();
            expect(notificationService.success).not.toHaveBeenCalled();
            expect(result).toEqual(user);
        });

        it('should handle errors and rethrow', async () => {
            const error = new Error('update failed');
            userRepository.updateUser = vi.fn(async () => {
                throw error;
            });
            const original = mockUserDetails(mockUserCaptain());
            const updated = { ...original, firstName: 'New Name' };

            try {
                await testee.updateUser(original, updated);
                expect(true).toBe(false); // fail test when promise was not rejected
            } catch (e) {
                expect(e).toBe(error);
                expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
            }
        });
    });

    describe('deleteUserByKey', () => {
        it('should delete user from repository, remove from cache and show success', async () => {
            await testee.deleteUserByKey('user-1');

            expect(userRepository.deleteUser).toHaveBeenCalledWith('user-1');
            expect(userCachingService.removeFromCache).toHaveBeenCalledWith('user-1');
            expect(notificationService.success).toHaveBeenCalled();
        });

        it('should handle errors and rethrow', async () => {
            const error = new Error('delete failed');
            userRepository.deleteUser = vi.fn(async () => {
                throw error;
            });

            try {
                await testee.deleteUserByKey('user-1');
                expect(true).toBe(false); // fail test when promise was not rejected
            } catch (e) {
                expect(e).toBe(error);
                expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
                expect(notificationService.success).not.toHaveBeenCalled();
            }
        });
    });

    describe('getUserDetailsByKey', () => {
        it('should return user details from repository', async () => {
            const details = mockUserDetails(mockUserCaptain());
            userRepository.findByKey = vi.fn(async () => details);

            const result = await testee.getUserDetailsByKey('captain-key');

            expect(userRepository.findByKey).toHaveBeenCalledWith('captain-key');
            expect(result).toEqual(details);
        });

        it('should handle errors and rethrow', async () => {
            const error = new Error('not found');
            userRepository.findByKey = vi.fn(async () => {
                throw error;
            });

            try {
                await testee.getUserDetailsByKey('unknown-key');
                expect(true).toBe(false); // fail test when promise was not rejected
            } catch (e) {
                expect(e).toBe(error);
                expect(errorHandlingService.handleRawError).toHaveBeenCalledWith(error);
            }
        });
    });

    describe('contactUsers', () => {
        beforeEach(() => vi.useFakeTimers());
        afterEach(() => vi.useRealTimers());

        it('should open mailto link for user with email', async () => {
            const promise = testee.contactUsers([mockUserCaptain()]);
            await vi.runAllTimersAsync();
            await promise;

            expect(errorHandlingService.handleError).not.toHaveBeenCalled();
        });

        it('should show error when single user has no email', async () => {
            await testee.contactUsers([mockUserCaptain({ email: undefined })]);

            expect(errorHandlingService.handleError).toHaveBeenCalledWith(
                expect.objectContaining({ title: 'Keine Email Adresse hinterlegt' })
            );
        });

        it('should show error with retry when some users in batch have no email', async () => {
            const withoutEmail = mockUserDeckhand({ email: undefined });

            await testee.contactUsers([mockUserCaptain(), withoutEmail]);

            expect(errorHandlingService.handleError).toHaveBeenCalledWith(expect.objectContaining({ retry: expect.any(Function) }));
        });

        it('should contact users with email and skip those without when ignoreMissingEmails is true', async () => {
            const promise = testee.contactUsers([mockUserCaptain(), mockUserDeckhand({ email: undefined })], true);
            await vi.runAllTimersAsync();
            await promise;

            expect(errorHandlingService.handleError).not.toHaveBeenCalled();
        });
    });

    describe('validateForCreate', () => {
        it('should return no errors for a valid user', () => {
            const result = testee.validateForCreate(mockUserCaptain());

            expect(Object.keys(result)).toHaveLength(0);
        });

        it('should return errors when first name is missing', () => {
            const result = testee.validateForCreate(mockUserCaptain({ firstName: '' }));

            expect(result['firstName']).toBeDefined();
        });

        it('should return errors when email is invalid', () => {
            const result = testee.validateForCreate(mockUserCaptain({ email: 'not-an-email' }));

            expect(result['email']).toBeDefined();
        });
    });

    describe('validate', () => {
        it('should return no errors for a valid user details', () => {
            const result = testee.validate(mockUserDetails(mockUserCaptain()));

            expect(Object.keys(result)).toHaveLength(0);
        });

        it('should return errors when first name is missing', () => {
            const result = testee.validate({ ...mockUserDetails(mockUserCaptain()), firstName: '' });

            expect(result['firstName']).toBeDefined();
        });

        it('should return errors when phone number is invalid', () => {
            const result = testee.validate({ ...mockUserDetails(mockUserCaptain()), phone: 'not-a-phone' });

            expect(result['phone']).toBeDefined();
        });
    });
});
