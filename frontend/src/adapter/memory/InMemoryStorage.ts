import type { CacheableEntity, Storage } from '@/application';

export class InMemoryStorage<K extends string | number, T extends CacheableEntity<K>> implements Storage<K, T> {
    private readonly items = new Map<K, T>();

    public async count(): Promise<number> {
        return this.items.size;
    }

    public async findAll(): Promise<T[]> {
        return [...this.items.values()];
    }

    public async findByKey(key: K): Promise<T | undefined> {
        return this.items.get(key);
    }

    public async save(entity: T): Promise<T> {
        this.items.set(entity.key, entity);
        return entity;
    }

    public async saveAll(entities: T[]): Promise<T[]> {
        for (const entity of entities) {
            this.items.set(entity.key, entity);
        }
        return entities;
    }

    public async deleteByKey(key: K): Promise<void> {
        this.items.delete(key);
    }

    public async delete(entity: T): Promise<void> {
        this.items.delete(entity.key);
    }

    public async deleteAll(): Promise<void> {
        this.items.clear();
    }
}
