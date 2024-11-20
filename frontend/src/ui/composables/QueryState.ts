import { watch } from 'vue';
import { useRouter } from 'vue-router';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useQueryStateSync<T>(prop: string, get: () => T, set: (t: T) => void) {
    const router = useRouter();

    async function updateQuery(): Promise<void> {
        const value = get();
        const route = router.currentRoute.value;
        if (route.query[prop] === value) {
            // nothing to do
            return;
        }
        if (value !== undefined && value !== null) {
            // update the query param
            route.query[prop] = encodeURIComponent(String(value));
        } else {
            // remove the query param
            // eslint-disable-next-line @typescript-eslint/no-dynamic-delete
            delete route.query[prop];
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
        const value = get();
        if (typeof value === 'number') {
            const param = parseInt(route.query[prop] as string, 10);
            if (!Number.isNaN(param)) {
                set(param as T);
            }
        } else {
            const param = route.query[prop] as string;
            if (param) {
                const decoded = decodeURIComponent(param);
                set(decoded as T);
            }
        }
    }

    readFromQuery();
    watch(get, updateQuery);
    return {};
}
