import { deepCopy } from '@/common';
import type { Cache, CacheableEntity } from '../../application/ports/Cache.ts';

export interface CacheInvalidationOptions {
    invalidateOnReload: boolean;
    invalidateOnInterval: number;
}

/**
 * IndexedDB Repository for a single IndexedDB store. The stores have to be created before constructing this class or
 * an error will be thrown. Example Implementation:
 * ````typescript
 * const databaseName = 'example';
 * const storeNames = ['store-a', 'store-b', 'store-c'];
 * const database = getConnection(databaseName, storeNames, 1);
 * const cacheA = new IndexedDBRepository<string, TypeA>(database, 'store-a');
 * const cacheB = new IndexedDBRepository<string, TypeA>(database, 'store-b');
 * const cacheC = new IndexedDBRepository<string, TypeA>(database, 'store-c');
 * ````
 */
export class IndexedDBRepository<K extends string | number, T extends CacheableEntity<K>> implements Cache<K, T> {
    private readonly database: Promise<IDBDatabase>;
    private readonly store: string;
    private readonly defaultCacheInvalidationOptions: CacheInvalidationOptions = {
        invalidateOnReload: true,
        invalidateOnInterval: 1000 * 60 * 60 * 6, // invalidate cache every 6 hours
    };
    private readonly cacheInvalidationOptions: CacheInvalidationOptions;

    /**
     * Create an IndexedDB repository for the specified store. The database passed may be still connecting. All
     * operations on the db will await the connection promise.
     * @param database promise of the IndexedDN connection
     * @param store name of the IndexedDB store, store musst be created manually before connecting!
     * @param cacheInvalidationOptions optional invalidation parameters
     */
    constructor(database: Promise<IDBDatabase>, store: string, cacheInvalidationOptions?: Partial<CacheInvalidationOptions>) {
        this.database = database;
        this.store = store;
        this.cacheInvalidationOptions = Object.assign(this.defaultCacheInvalidationOptions, cacheInvalidationOptions || {});
        if (this.cacheInvalidationOptions.invalidateOnReload) {
            this.deleteAll();
        }
        if (this.cacheInvalidationOptions.invalidateOnInterval > 0) {
            setInterval(() => this.deleteAll(), this.cacheInvalidationOptions.invalidateOnInterval);
        }
    }

    /**
     * Get the number of cached entries
     */
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

    /**
     * Return all entries saved in this store. If the store is empty, an empty array will be returned
     */
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

    /**
     * Find an entry by key and return it if present
     * @param key
     */
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

    /**
     * Create or update an entry. If another entry exists with the same key, that other entry will be overwritten.
     * @param entity
     */
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

    /**
     * Create or update all entries. In case of a key collision the new entry wins.
     * @param entities
     */
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

    /**
     * Delete the entry with the specified key
     * @param key
     */
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

    /**
     * Remove the entry
     * @param entity
     */
    public async delete(entity: T): Promise<void> {
        return this.deleteByKey(entity.key);
    }

    /**
     * Clear the store
     */
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
