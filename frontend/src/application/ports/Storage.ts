export interface CacheableEntity<K extends string | number = string> {
    key: K;
}

export interface Storage<K extends string | number, T extends CacheableEntity<K>> {
    /**
     * Get the number of stored items
     * @return item count
     */
    count(): Promise<number>;

    /**
     * Return all items saved in this store. If the store is empty, an empty array will be returned
     * @return all stored items
     */
    findAll(): Promise<T[]>;

    /**
     * Find an item by key and return it if present
     * @param key
     * @return the item or undefined
     */
    findByKey(key: K): Promise<T | undefined>;

    /**
     * Create or update an item. If another item exists with the same key, that item will be overwritten.
     * @param entity
     * @return the saved item
     */
    save(entity: T): Promise<T>;

    /**
     * Create or update all items. In case of a key collision the new item wins.
     * @param entities
     * @return the saved items
     */
    saveAll(entities: T[]): Promise<T[]>;

    /**
     * Delete the item with the specified key
     * @param key
     */
    deleteByKey(key: K): Promise<void>;

    /**
     * Remove the item
     * @param entity
     */
    delete(entity: T): Promise<void>;

    /**
     * Delete all stored items
     */
    deleteAll(): Promise<void>;
}
