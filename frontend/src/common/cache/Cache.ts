export interface CacheableEntity<K extends string | number = string> {
    key: K;
}

export interface Cache<K extends string | number, T extends CacheableEntity<K>> {
    /**
     * Get the number of cached entries
     */
    count(): Promise<number>;

    /**
     * Return all entries saved in this store. If the store is empty, an empty array will be returned
     */
    findAll(): Promise<T[]>;

    /**
     * Find an entry by key and return it if present
     * @param key
     */
    findByKey(key: K): Promise<T | undefined>;

    /**
     * Create or update an entry. If another entry exists with the same key, that other entry will be overwritten.
     * @param entity
     */
    save(entity: T): Promise<T>;

    /**
     * Create or update all entries. In case of a key collision the new entry wins.
     * @param entities
     */
    saveAll(entities: T[]): Promise<T[]>;

    /**
     * Delete the entry with the specified key
     * @param key
     */
    deleteByKey(key: K): Promise<void>;

    /**
     * Remove the entry
     * @param entity
     */
    delete(entity: T): Promise<void>;

    /**
     * Clear the cache
     */
    deleteAll(): Promise<void>;
}
