import type { CacheableEntity, Storage } from '@/application/ports';
import { deepCopy } from '@/common';

/**
 * IndexedDB adapter for a single IndexedDB store. The stores have to be created before constructing this class or
 * an error will be thrown. Example Implementation:
 * ````typescript
 * const databaseName = 'example';
 * const storeNames = ['store-a', 'store-b', 'store-c'];
 * const database = getConnection(databaseName, storeNames, 1);
 * const storeA = new IndexedDBStorage<string, TypeA>(database, 'store-a');
 * const storeB = new IndexedDBStorage<string, TypeB>(database, 'store-b');
 * const storeC = new IndexedDBStorage<string, TypeC>(database, 'store-c');
 * ````
 */
export class IndexedDBStorage<K extends string | number, T extends CacheableEntity<K>> implements Storage<K, T> {
    private readonly database: Promise<IDBDatabase>;
    private readonly store: string;

    /**
     * Create an IndexedDB adapter for the specified store. The database passed may be still connecting. All
     * operations on the db will await the connection promise.
     * @param database promise of the IndexedDN connection
     * @param store name of the IndexedDB store, store must be created manually before connecting!
     */
    constructor(database: Promise<IDBDatabase>, store: string) {
        this.database = database;
        this.store = store;
    }

    public async count(): Promise<number> {
        const database = await this.database;
        return new Promise<number>((resolve, reject) => {
            const transaction = database.transaction(this.store, 'readonly');
            const store = transaction.objectStore(this.store);
            const query = store.count();
            query.onsuccess = (): void => resolve(query.result);
            query.onerror = (): void => reject(query.error);
        });
    }

    public async findAll(): Promise<T[]> {
        const database = await this.database;
        return new Promise<T[]>((resolve, reject) => {
            const transaction = database.transaction(this.store, 'readonly');
            const store = transaction.objectStore(this.store);
            const query = store.getAll();
            query.onsuccess = (): void => resolve(query.result.map((it) => it.value));
            query.onerror = (): void => reject(query.error);
        });
    }

    public async findByKey(key: K): Promise<T | undefined> {
        if (!key) {
            return undefined;
        }
        const database = await this.database;
        return new Promise<T | undefined>((resolve, reject) => {
            const transaction = database.transaction(this.store, 'readonly');
            const store = transaction.objectStore(this.store);
            const query = store.get(key);
            query.onsuccess = (): void => {
                if (query.result && query.result.value) {
                    resolve(query.result.value);
                }
                resolve(undefined);
            };
            query.onerror = (): void => reject(query.error);
        });
    }

    public async save(entity: T): Promise<T> {
        const clone = deepCopy(entity);
        const database = await this.database;
        return new Promise<T>((resolve, reject) => {
            const transaction = database.transaction(this.store, 'readwrite');
            const store = transaction.objectStore(this.store);
            const query = store.put({
                key: clone.key,
                updated: new Date(),
                value: clone,
            });
            query.onsuccess = (): void => resolve(clone);
            query.onerror = (): void => reject(query.error);
        });
    }

    public async saveAll(entities: T[]): Promise<T[]> {
        if (entities.length === 0) {
            return [];
        }
        const database = await this.database;
        return new Promise<T[]>((resolve, reject) => {
            const transaction = database.transaction(this.store, 'readwrite');
            const store = transaction.objectStore(this.store);
            entities.forEach((entity) => {
                store.put({
                    key: entity.key,
                    updated: new Date(),
                    value: entity,
                });
            });
            transaction.oncomplete = (): void => resolve(entities);
            transaction.onerror = (): void => reject(transaction.error);
        });
    }

    public async deleteByKey(key: K): Promise<void> {
        const database = await this.database;
        return new Promise<void>((resolve, reject) => {
            const transaction = database.transaction(this.store, 'readwrite');
            const store = transaction.objectStore(this.store);
            const query = store.delete(key);
            query.onsuccess = (): void => resolve();
            query.onerror = (): void => reject(query.error);
        });
    }

    public async delete(entity: T): Promise<void> {
        return this.deleteByKey(entity.key);
    }

    public async deleteAll(): Promise<void> {
        const database = await this.database;
        return new Promise<void>((resolve, reject) => {
            const transaction = database.transaction(this.store, 'readwrite');
            const store = transaction.objectStore(this.store);
            const query = store.clear();
            query.onsuccess = (): void => resolve();
            query.onerror = (): void => reject(query.error);
        });
    }
}
