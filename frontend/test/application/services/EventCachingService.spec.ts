import { beforeEach, describe, expect, it, vi } from 'vitest';
import { InMemoryStorage } from '@/adapter/memory/InMemoryStorage';
import type { EventRepository, Storage } from '@/application/ports';
import { EventCachingService } from '@/application/services';
import type { Event } from '@/domain';
import { mockEvent } from '~/mocks';

describe('EventCachingService', () => {
    let testee: EventCachingService;
    let eventRepository: EventRepository;
    let cache: Storage<string, Event>;

    beforeEach(() => {
        vi.useFakeTimers();
        vi.setSystemTime(new Date('2026-07-01T12:00:00Z'));

        cache = new InMemoryStorage<string, Event>();
        eventRepository = { findAll: vi.fn(async () => []) } as unknown as EventRepository;
    });

    it('should prefetch +/-2 years', async () => {
        testee = new EventCachingService({ eventRepository, cache });
        expect(eventRepository.findAll).toHaveBeenCalledWith(2024);
        expect(eventRepository.findAll).toHaveBeenCalledWith(2025);
        expect(eventRepository.findAll).toHaveBeenCalledWith(2026);
        expect(eventRepository.findAll).toHaveBeenCalledWith(2027);
        expect(eventRepository.findAll).toHaveBeenCalledWith(2028);
    });

    it('should return fetched events', async () => {
        const byYear = new Map<number, Event[]>([
            [2024, [mockEvent({ key: 'e24', start: new Date('2024-01-01T00:00:00Z'), end: new Date('2024-01-01T00:00:00Z') })]],
            [2025, [mockEvent({ key: 'e25', start: new Date('2025-01-01T00:00:00Z'), end: new Date('2025-01-01T00:00:00Z') })]],
            [2026, [mockEvent({ key: 'e26', start: new Date('2026-01-01T00:00:00Z'), end: new Date('2026-01-01T00:00:00Z') })]],
            [2027, [mockEvent({ key: 'e27', start: new Date('2027-01-01T00:00:00Z'), end: new Date('2027-01-01T00:00:00Z') })]],
            [2028, [mockEvent({ key: 'e28', start: new Date('2028-01-01T00:00:00Z'), end: new Date('2028-01-01T00:00:00Z') })]],
        ]);
        eventRepository.findAll = vi.fn(async (year: number) => byYear.get(year) ?? []);
        testee = new EventCachingService({ eventRepository, cache });
        const events = await testee.getEvents(2026);
        expect(events).toEqual(byYear.get(2026));
    });

    it('should return empty list for already fetched year when cache is empty', async () => {
        const testee = new EventCachingService({ eventRepository, cache });
        const events = await testee.getEvents(2026);
        expect(events).toEqual([]);
    });

    it('should fetch years outside of prefetch window on demand', async () => {
        eventRepository.findAll = vi.fn(async (year: number) => {
            if (year === 2029) {
                return [mockEvent({ key: 'future', start: new Date('2029-06-01T00:00:00Z'), end: new Date('2029-06-02T00:00:00Z') })];
            }
            return [];
        });
        testee = new EventCachingService({ eventRepository, cache });
        const events = await testee.getEvents(2029);
        expect(events).toHaveLength(1);
        expect(events[0].key).toBe('future');
        expect(eventRepository.findAll).toHaveBeenLastCalledWith(2029);
    });

    it('should clear local cache when initialization fails with unauthorized response', async () => {
        eventRepository.findAll = vi.fn(async () => {
            throw { status: 401 };
        });
        await cache.saveAll([mockEvent({ key: 'stale', start: new Date('2026-01-01T00:00:00Z'), end: new Date('2026-01-01T00:00:00Z') })]);

        testee = new EventCachingService({ eventRepository, cache });
        await testee.getEvents(2026);

        expect(await cache.findAll()).toEqual([]);
    });

    it('should only update cache when events of same year are already cached', async () => {
        eventRepository.findAll = vi.fn(async (year: number) => {
            if (year === 2026) {
                return [mockEvent({ key: 'existing', start: new Date('2026-01-10T00:00:00Z'), end: new Date('2026-01-11T00:00:00Z') })];
            }
            return [];
        });
        testee = new EventCachingService({ eventRepository, cache });
        await testee.getEvents(2026);

        const saved = await testee.updateCache(
            mockEvent({ key: 'new-2026', start: new Date('2026-09-01T00:00:00Z'), end: new Date('2026-09-02T00:00:00Z') })
        );
        const skipped = await testee.updateCache(
            mockEvent({ key: 'new-2035', start: new Date('2035-09-01T00:00:00Z'), end: new Date('2035-09-02T00:00:00Z') })
        );

        expect(saved.key).toBe('new-2026');
        expect(skipped.key).toBe('new-2035');
        const cached = await cache.findAll();
        expect(cached.some((it) => it.key === 'new-2026')).toBe(true);
        expect(cached.some((it) => it.key === 'new-2035')).toBe(false);
    });
});
