/**
 * Get a IndexedDB Connection and create the passed stores, when they don't already exist. When changing or adding
 * a store, the version needs to be updated manually!
 *
 * ````typescript
 * const databaseName = 'example';
 * const storeNames = ['store-a', 'store-b', 'store-c'];
 * const database = IndexedDB.getConnection(databaseName, storeNames, 1);
 * const storeA = new IndexedDBStorage<string, TypeA>(database, 'store-a');
 * const storeB = new IndexedDBStorage<string, TypeB>(database, 'store-b');
 * const storeC = new IndexedDBStorage<string, TypeC>(database, 'store-c');
 * ````
 * @param databaseName
 * @param storeNames
 * @param version
 */
export async function getConnection(databaseName: string, storeNames: string[], version = 1): Promise<IDBDatabase> {
    // console.log(`🗄️ Connecting to IndexedDB ${databaseName}`);
    return new Promise<IDBDatabase>((resolve, reject) => {
        const request = globalThis.indexedDB.open(databaseName, version);
        request.onsuccess = (): void => {
            const db = request.result;
            const missingStores = storeNames.filter((store) => !db.objectStoreNames.contains(store));
            if (missingStores.length === 0) {
                resolve(request.result);
            } else {
                reject(new Error(`Missing stores: ${missingStores.join(', ')}. Increase DB Version to create new stores.`));
            }
        };
        request.onerror = async (): Promise<void> => {
            await deleteDatabase(databaseName);
            const retry = await getConnection(databaseName, storeNames, version);
            resolve(retry);
        };
        request.onupgradeneeded = (): void => {
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
export async function deleteDatabase(databaseName: string): Promise<void> {
    // console.log(`🗄️ Deleting IndexedDB ${databaseName}`);
    return new Promise((resolve, reject) => {
        const deleteRequest = globalThis.indexedDB.deleteDatabase(databaseName);
        deleteRequest.onsuccess = (): void => resolve();
        deleteRequest.onerror = (): void => reject();
    });
}
