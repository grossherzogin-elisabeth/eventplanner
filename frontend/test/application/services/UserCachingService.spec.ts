import { beforeEach, describe, expect, it, vi } from 'vitest';
import { InMemoryStorage } from '@/adapter/memory/InMemoryStorage';
import type { Storage, UserRepository } from '@/application/ports';
import { UserCachingService } from '@/application/services';
import type { User } from '@/domain';
import { mockUserEngineer, mockUserMate } from '~/mocks';

describe('UserCachingService', () => {
    let testee: UserCachingService;
    let userRepository: UserRepository;
    let cache: Storage<string, User>;

    beforeEach(() => {
        userRepository = { findAll: vi.fn(async () => []) } as unknown as UserRepository;
        cache = new InMemoryStorage<string, User>();
    });

    it('should initialize from repository and return users sorted by first and last name', async () => {
        userRepository.findAll = vi.fn(async () => [
            mockUserMate({ key: 'b', firstName: 'Max', lastName: 'B' }),
            mockUserEngineer({ key: 'a', firstName: 'Anna', lastName: 'A' }),
        ]);
        testee = new UserCachingService({ userRepository, cache });

        const users = await testee.getUsers();

        expect(users.map((it) => it.key)).toEqual(['a', 'b']);
        expect(userRepository.findAll).toHaveBeenCalledTimes(1);
    });

    it('should clear stale data on unauthorized initialization errors', async () => {
        userRepository.findAll = vi.fn().mockRejectedValueOnce({ status: 403 }).mockResolvedValueOnce([]);
        await cache.saveAll([mockUserMate({ key: 'stale' })]);
        testee = new UserCachingService({ userRepository, cache });

        const users = await testee.getUsers();

        expect(users).toEqual([]);
        expect(await cache.findAll()).toEqual([]);
    });

    it('should filter by keys and update cached users without losing missing properties', async () => {
        userRepository.findAll = vi.fn(async () => [
            mockUserEngineer({ key: 'u1', firstName: 'Alice', lastName: 'Zephyr', nickName: 'Al' }),
            mockUserMate({ key: 'u2', firstName: 'Bob', lastName: 'Yellow' }),
        ]);
        testee = new UserCachingService({ userRepository, cache });
        const filtered = await testee.getUsers(['u2']);
        const updated = await testee.updateCache({
            key: 'u1',
            firstName: 'Alice',
            lastName: 'Aardvark',
            email: 'user@example.com',
            positionKeys: [],
            roles: [],
            qualifications: [],
        });

        expect(filtered.map((it) => it.key)).toEqual(['u2']);
        expect(updated.nickName).toBe('Al');
        expect((await cache.findByKey('u1'))?.lastName).toBe('Aardvark');
    });
});
