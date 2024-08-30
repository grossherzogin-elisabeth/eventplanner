import { ObjectUtils } from '@/common';
import type { Cache, CacheableEntity } from '../Cache';

export interface StoreChangedEvent<K extends string | number = string> {
    action: 'update' | 'remove' | 'clear';
    keys: K[];
    store: string;
}

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
 * const database = IndexedDB.getConnection(databaseName, storeNames, 1);
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
    private readonly listeners: ((event: StoreChangedEvent<K>) => void)[] = [];

    /**
     * Create an IndexedDB repository for the specified store. The database passed may be still connecting. All
     * operations on the db will await the connection promise.
     * @param database promise of the IndexedDN connection
     * @param store name of the IndexedDB store, store musst be created manually before connecting!
     * @param cacheInvalidationOptions optional invalidation parameters
     */
    constructor(
        database: Promise<IDBDatabase>,
        store: string,
        cacheInvalidationOptions?: Partial<CacheInvalidationOptions>
    ) {
        this.database = database;
        this.store = store;
        this.cacheInvalidationOptions = Object.assign(
            this.defaultCacheInvalidationOptions,
            cacheInvalidationOptions || {}
        );
        if (this.cacheInvalidationOptions.invalidateOnReload) {
            this.deleteAll();
        }
        if (this.cacheInvalidationOptions.invalidateOnInterval > 0) {
            setInterval(() => this.deleteAll(), this.cacheInvalidationOptions.invalidateOnInterval);
        }
    }

    /**
     * Register a listener to receive an event every time an entry is created, updated or deleted
     * @param callback
     */
    public addChangedListener(callback: (event: StoreChangedEvent<K>) => void): void {
        this.listeners.push(callback);
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
            query.onsuccess = () => resolve(query.result);
            query.onerror = () => reject(query.error);
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
            query.onsuccess = () => resolve(query.result.map((it) => it.value));
            query.onerror = () => reject(query.error);
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
            query.onsuccess = () => {
                if (query.result && query.result.value) {
                    resolve(query.result.value);
                }
                resolve(undefined);
            };
            query.onerror = () => reject(query.error);
        });
    }

    /**
     * Create or update an entry. If another entry exists with the same key, that other entry will be overwritten.
     * @param entity
     */
    public async save(entity: T): Promise<T> {
        const clone = ObjectUtils.deepCopy(entity);
        const database = await this.database;
        return new Promise<T>((resolve, reject) => {
            const transaction = database.transaction(this.store, 'readwrite');
            const store = transaction.objectStore(this.store);
            const query = store.put({
                key: clone.key,
                updated: new Date(),
                value: clone,
            });
            query.onsuccess = () => {
                resolve(clone);
                this.emitChangeEvent('update', [clone.key]);
            };
            query.onerror = () => reject(query.error);
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
            transaction.oncomplete = () => {
                resolve(entities);
                this.emitChangeEvent(
                    'update',
                    entities.map((e) => e.key)
                );
            };
            transaction.onerror = () => reject(transaction.error);
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
            query.onsuccess = () => {
                resolve();
                this.emitChangeEvent('remove', [key]);
            };
            query.onerror = () => reject(query.error);
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
        const allEntities = await this.findAll();
        return new Promise<void>((resolve, reject) => {
            const transaction = database.transaction(this.store, 'readwrite');
            const store = transaction.objectStore(this.store);
            const query = store.clear();
            query.onsuccess = () => {
                resolve();
                this.emitChangeEvent(
                    'clear',
                    allEntities.map((e) => e.key)
                );
            };
            query.onerror = () => reject(query.error);
        });
    }

    private async emitChangeEvent(action: 'update' | 'remove' | 'clear', keys: K[]): Promise<void> {
        if (keys.length > 0) {
            const event = {
                store: this.store,
                action: action,
                keys: keys,
            };
            this.listeners.forEach((callback) => {
                try {
                    callback(event);
                } catch (e) {
                    console.error(e);
                }
            });
        }
    }
}
