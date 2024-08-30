/**
 * Promise wrapper for an IndexedDB Connection to be used with IndexedDBRepository. Example Implementation:
 *
 * ````typescript
 * const databaseName = 'example';
 * const storeNames = ['store-a', 'store-b', 'store-c'];
 * const database = IndexedDB.getConnection(databaseName, storeNames, 1);
 * const cacheA = new IndexedDBRepository<string, TypeA>(database, 'store-a');
 * const cacheB = new IndexedDBRepository<string, TypeA>(database, 'store-b');
 * const cacheC = new IndexedDBRepository<string, TypeA>(database, 'store-c');
 * ````
 */
export class IndexedDB {
    /**
     * Get a IndexedDB Connection and create the passed stores, when they don't already exist. When changing or adding
     * a store, the version needs to be updated manually!
     *
     * ````typescript
     * const databaseName = 'example';
     * const storeNames = ['store-a', 'store-b', 'store-c'];
     * const database = IndexedDB.getConnection(databaseName, storeNames, 1);
     * const cacheA = new IndexedDBRepository<string, TypeA>(database, 'store-a');
     * const cacheB = new IndexedDBRepository<string, TypeA>(database, 'store-b');
     * const cacheC = new IndexedDBRepository<string, TypeA>(database, 'store-c');
     * ````
     * @param databaseName
     * @param storeNames
     * @param version
     */
    public static async getConnection(
        databaseName: string,
        storeNames: string[],
        version: number = 1
    ): Promise<IDBDatabase> {
        // console.log(`🗄️ Connecting to IndexedDB ${databaseName}`);
        return new Promise<IDBDatabase>((resolve, reject) => {
            const request = window.indexedDB.open(databaseName, version);
            request.onsuccess = () => {
                const db = request.result;
                const missingStores = storeNames.filter((store) => !db.objectStoreNames.contains(store));
                if (missingStores.length === 0) {
                    resolve(request.result);
                } else {
                    reject(
                        new Error(
                            `Missing stores: ${missingStores.join(', ')}. Increase DB Version to create new stores.`
                        )
                    );
                }
            };
            request.onerror = async () => {
                await IndexedDB.deleteDatabase(databaseName);
                const retry = await IndexedDB.getConnection(databaseName, storeNames, version);
                resolve(retry);
            };
            request.onupgradeneeded = () => {
                const database = request.result;
                // drop all stores
                const stores = database.objectStoreNames;
                for (let i = 0; i < stores.length; i++) {
                    const store = stores.item(i);
                    if (store) {
                        database.deleteObjectStore(store);
                    }
                }
                // recreate stores
                storeNames.forEach((storeName) => database.createObjectStore(storeName, { keyPath: 'key' }));
            };
        });
    }

    /**
     * Delete the IndexedDB with the specified name
     * @param databaseName
     */
    public static async deleteDatabase(databaseName: string): Promise<void> {
        // console.log(`🗄️ Deleting IndexedDB ${databaseName}`);
        return new Promise((resolve, reject) => {
            const deleteRequest = window.indexedDB.deleteDatabase(databaseName);
            deleteRequest.onsuccess = () => resolve();
            deleteRequest.onerror = () => reject();
        });
    }
}
