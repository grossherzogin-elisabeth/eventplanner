import { beforeEach, describe, expect, it } from 'vitest';
import { getConnection } from '@/adapter/indexeddb/IndexedDB.ts';
import { IndexedDBStorage } from '@/adapter/indexeddb/IndexedDBStorage.ts';
import { InMemoryStorage } from '@/adapter/memory/InMemoryStorage.ts';
import type { Storage } from '@/application/ports';

interface ExampleType {
    key: string;
    stringAttribute: string;
    numericAttribute: number;
}

const itemA: ExampleType = { key: 'a', stringAttribute: 'Test 1', numericAttribute: 123 };
const itemB: ExampleType = { key: 'b', stringAttribute: 'Test 2', numericAttribute: 123 };
const itemC: ExampleType = { key: 'c', stringAttribute: 'Test 3', numericAttribute: 123 };
const itemD: ExampleType = { key: 'd', stringAttribute: 'Test 4', numericAttribute: 123 };

const implementations: { name: string; instance: () => Storage<string, ExampleType> }[] = [
    { name: 'InMemoryStorage', instance: () => new InMemoryStorage() },
    { name: 'IndexedDbStorage', instance: () => new IndexedDBStorage(getConnection('test', ['test']), 'test') },
];

describe.each(implementations)('$name', ({ instance }) => {
    let testee: Storage<string, ExampleType> = instance();

    beforeEach(async () => {
        testee = instance();
        await testee.deleteAll();
        await testee.saveAll([itemA, itemB, itemC, itemD]);
    });

    it('should read all items', async () => {
        const result = await testee.findAll();
        expect(result).toHaveLength(4);
    });

    it('should read item by key', async () => {
        const result = await testee.findByKey('b');
        expect(result).toEqual(itemB);
    });

    it('should save new item', async () => {
        const item = { key: 'e', stringAttribute: 'Test 5', numericAttribute: 123 };
        const result = await testee.save(item);
        expect(result).toEqual(item);
    });

    it('should save multiple items', async () => {
        const itemE = { key: 'e', stringAttribute: 'Test 5', numericAttribute: 123 };
        const itemF = { key: 'f', stringAttribute: 'Test 6', numericAttribute: 123 };
        const countBefore = await testee.count();

        const result = await testee.saveAll([itemE, itemF]);

        expect(result).toEqual([itemE, itemF]);
        expect(await testee.count()).toBe(countBefore + 2);
    });

    it('should delete item', async () => {
        await testee.delete(itemC);

        const all = await testee.findAll();
        expect(all).toHaveLength(3);
        expect(all).toContainEqual(itemA);
        expect(all).toContainEqual(itemB);
        expect(all).toContainEqual(itemD);
        expect(all).not.toContainEqual(itemC);
    });

    it('should delete by key', async () => {
        await testee.deleteByKey('c');

        const all = await testee.findAll();
        expect(all).toHaveLength(3);
        expect(all).toContainEqual(itemA);
        expect(all).toContainEqual(itemB);
        expect(all).toContainEqual(itemD);
        expect(all).not.toContainEqual(itemC);
    });

    it('should delete all', async () => {
        await testee.deleteAll();

        expect(await testee.findAll()).toEqual([]);
        expect(await testee.count()).toBe(0);
    });
});
