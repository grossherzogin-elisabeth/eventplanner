import type { Ref, UnwrapRef } from 'vue';
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { deepCopy } from '@/common';

export interface UseQueryState<T> {
    parameter: Ref<T> | Ref<UnwrapRef<T>>;
    clearWatchers(): void;
}

export function useQuery<T = string | number | boolean | string[]>(name: string, defaultValue: T): UseQueryState<T> {
    const router = useRouter();

    const parameter = ref<T>(deepCopy(defaultValue));

    function load(): void {
        const query = getQueryParameter() ?? defaultValue;
        // a simple comparison query === parameter.value can produce false negatives on arrays, that can result in an
        // endless loop, so we compare the JSON.stringify result of the two
        if (JSON.stringify(query) !== JSON.stringify(parameter.value)) {
            parameter.value = query;
        }
    }

    async function setQueryParameter(value: T): Promise<void> {
        const route = router.currentRoute.value;
        if (Array.isArray(value) && value.length === 0) {
            // eslint-disable-next-line @typescript-eslint/no-dynamic-delete
            delete route.query[name];
        } else if (value !== undefined && value !== null && JSON.stringify(value) !== JSON.stringify(defaultValue)) {
            // update the query param
            if (Array.isArray(value)) {
                route.query[name] = encodeURIComponent(value.join(','));
            } else {
                route.query[name] = encodeURIComponent(String(value));
            }
        } else {
            // remove the query param
            // eslint-disable-next-line @typescript-eslint/no-dynamic-delete
            delete route.query[name];
        }

        if (route.name) {
            await router.replace({
                name: route.name || undefined,
                hash: route.hash,
                params: route.params,
                query: route.query,
                force: true,
            });
        } else {
            await router.replace({
                hash: route.hash,
                path: route.path,
                query: route.query,
                force: true,
            });
        }
    }

    function getQueryParameter(): T | undefined {
        const route = router.currentRoute.value;
        if (typeof defaultValue === 'number') {
            const value = Number.parseInt(route.query[name] as string, 10);
            if (!Number.isNaN(value)) {
                return value as T;
            }
        } else if (typeof defaultValue === 'boolean') {
            const value = route.query[name] as string;
            if (value !== undefined && value !== null) {
                return (value === 'true') as T;
            }
        } else if (Array.isArray(defaultValue)) {
            const value = route.query[name] as string;
            if (value !== undefined && value !== null) {
                return decodeURIComponent(value)
                    .split(',')
                    .filter((it) => it.trim().length > 0) as T;
            }
        } else if (typeof defaultValue === 'string') {
            const value = route.query[name] as string;
            if (value !== undefined && value !== null) {
                return decodeURIComponent(value) as T;
            }
        }
        return undefined;
    }

    load();

    const unwatchParameter = watch(parameter, () => setQueryParameter(parameter.value), { deep: true });
    const unwatchRoute = watch(() => router.currentRoute, load, { deep: true });

    function clearWatchers(): void {
        unwatchParameter();
        unwatchRoute();
    }

    return {
        parameter,
        clearWatchers,
    };
}
