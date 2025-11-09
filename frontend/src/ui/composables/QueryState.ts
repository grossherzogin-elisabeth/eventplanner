import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useQuery<T = string | number | boolean | string[]>(name: string, defaultValue: T) {
    const router = useRouter();

    const parameter = ref<T>(defaultValue);

    async function updateQuery(): Promise<void> {
        console.log('update query');
        const value = parameter.value;
        const route = router.currentRoute.value;
        if (value !== undefined && value !== null && value !== defaultValue) {
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

    function readFromQuery(): void {
        const route = router.currentRoute.value;
        if (typeof defaultValue === 'number') {
            const value = Number.parseInt(route.query[name] as string, 10);
            if (!Number.isNaN(value)) {
                parameter.value = value as T;
            }
        } else if (typeof defaultValue === 'boolean') {
            const value = route.query[name] as string;
            if (value !== undefined && value !== null) {
                parameter.value = value === 'true';
            }
        } else if (Array.isArray(defaultValue)) {
            const value = route.query[name] as string;
            if (value !== undefined && value !== null) {
                parameter.value = decodeURIComponent(value)
                    .split(',')
                    .filter((it) => it.trim().length > 0);
            }
        } else if (typeof defaultValue === 'string') {
            const value = route.query[name] as string;
            if (value !== undefined && value !== null) {
                parameter.value = decodeURIComponent(value) as T;
            }
        }
    }

    readFromQuery();
    watch(() => parameter, updateQuery, { deep: true });
    return {
        parameter,
    };
}
