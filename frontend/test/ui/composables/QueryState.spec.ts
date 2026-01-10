import type { Ref } from 'vue';
import { nextTick, ref } from 'vue';
import type { RouteLocationNormalizedLoadedGeneric, Router } from 'vue-router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useQuery } from '@/ui/composables/QueryState';
import { withSetup } from '~/utils/withSetup.ts';

const mockRoute = ref<RouteLocationNormalizedLoadedGeneric>({
    name: 'test-route',
    path: '/test',
    hash: '',
    params: {},
    query: {},
    fullPath: '/test',
    matched: [],
    meta: {},
    redirectedFrom: undefined,
});

// we mock `router.replace` to track its calls and update the `mockRoute`.
const mockRouterReplace = vi.fn(async (to) => {
    // simulate router navigation by updating mockRoute.value.
    // this is crucial for the `watch(() => router.currentRoute, load)` effect to work.
    const newQuery = typeof to === 'string' ? {} : to.query || {};
    const newPath = typeof to === 'string' ? to : to.path || mockRoute.value.path;
    const newName = typeof to === 'string' ? mockRoute.value.name : to.name || mockRoute.value.name;
    const newHash = typeof to === 'string' ? mockRoute.value.hash : to.hash || mockRoute.value.hash;
    const newParams = typeof to === 'string' ? mockRoute.value.params : to.params || mockRoute.value.params;

    mockRoute.value = {
        ...mockRoute.value,
        query: newQuery,
        path: newPath,
        name: newName,
        hash: newHash,
        params: newParams,
        // fullPath is simplified here as it's not relevant for these tests
        fullPath: `${newPath}?${new URLSearchParams(newQuery as Record<string, string>).toString()}`,
    };
    await nextTick();
});

vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => ({
        currentRoute: mockRoute,
        replace: mockRouterReplace,
    }),
}));

describe('useQuery', () => {
    let testee: { parameter: Ref; clearWatchers: () => void };

    beforeEach(() => {
        mockRoute.value.query = {};
        vi.clearAllMocks();
    });

    afterEach(() => {
        testee.clearWatchers();
    });

    describe('string parameters', () => {
        it('should load the default value if the parameter is not present in the URL', () => {
            testee = withSetup(() => useQuery('search', 'default')).instance;
            expect(testee.parameter.value).toBe('default');
            expect(mockRouterReplace).not.toHaveBeenCalled();
        });

        it('should load the value from the URL if the parameter is present', () => {
            mockRoute.value.query = { search: 'hello' };
            testee = withSetup(() => useQuery('search', 'default')).instance;
            expect(testee.parameter.value).toBe('hello');
            expect(mockRouterReplace).not.toHaveBeenCalled();
        });

        it('should load and decode the value from the URL', () => {
            mockRoute.value.query = { search: encodeURIComponent('hello world') };
            testee = withSetup(() => useQuery('search', 'default')).instance;
            expect(testee.parameter.value).toBe('hello world');
        });

        it('should update the URL parameter when the parameter ref changes', async () => {
            testee = withSetup(() => useQuery('search', 'default')).instance;
            testee.parameter.value = 'new_value';
            await nextTick();
            expect(mockRouterReplace).toHaveBeenCalledTimes(1);
            expect(mockRouterReplace).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: { search: 'new_value' },
                })
            );
            expect(mockRoute.value.query.search).toBe('new_value');
        });

        it('should remove the URL parameter when the parameter ref is set to the default value', async () => {
            mockRoute.value.query = { search: 'existing_value' };
            testee = withSetup(() => useQuery('search', 'default')).instance;
            expect(testee.parameter.value).toBe('existing_value');
            testee.parameter.value = 'default';
            await nextTick();
            expect(mockRouterReplace).toHaveBeenCalledTimes(1);
            expect(mockRouterReplace).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: {},
                })
            );
            expect(mockRoute.value.query.search).toBeUndefined();
        });

        it('should remove the URL parameter when the parameter ref is set to undefined', async () => {
            mockRoute.value.query = { search: 'existing_value' };
            testee = withSetup(() => useQuery<string | undefined>('search', 'default')).instance;
            expect(testee.parameter.value).toBe('existing_value');
            testee.parameter.value = undefined;
            await nextTick();
            expect(mockRoute.value.query.search).toBeUndefined();
        });

        it('should remove the URL parameter when the parameter ref is set to null', async () => {
            mockRoute.value.query = { search: 'existing_value' };
            testee = withSetup(() => useQuery<string | null>('search', 'default')).instance;
            expect(testee.parameter.value).toBe('existing_value');
            testee.parameter.value = null;
            await nextTick();
            expect(mockRoute.value.query.search).toBeUndefined();
        });

        it('should update the parameter ref when the URL parameter changes', async () => {
            testee = withSetup(() => useQuery('search', 'default')).instance;
            expect(testee.parameter.value).toBe('default');

            mockRoute.value = {
                ...mockRoute.value,
                query: { search: 'from_url' },
            };
            await nextTick();
            expect(testee.parameter.value).toBe('from_url');
        });

        it('should not call router.replace if the parameter ref is set to default and the URL parameter is already absent', async () => {
            mockRoute.value.query = {}; // No 'search' parameter
            testee = withSetup(() => useQuery('search', 'default')).instance;
            expect(testee.parameter.value).toBe('default'); // initial load sets to default
            mockRouterReplace.mockClear(); // clear any potential initial calls

            testee.parameter.value = 'default'; // setting to default again
            await nextTick();
            expect(mockRouterReplace).not.toHaveBeenCalled();
        });
    });

    describe('numeric parameters', () => {
        it('should load the default value if the parameter is not present in the URL', () => {
            testee = withSetup(() => useQuery('page', 1)).instance;
            expect(testee.parameter.value).toBe(1);
        });

        it('should load the value from the URL if the parameter is present', () => {
            mockRoute.value.query = { page: '5' };
            testee = withSetup(() => useQuery('page', 1)).instance;
            expect(testee.parameter.value).toBe(5);
        });

        it('should load the default value if the URL parameter is not a valid number', () => {
            mockRoute.value.query = { page: 'abc' };
            testee = withSetup(() => useQuery('page', 1)).instance;
            expect(testee.parameter.value).toBe(1);
        });

        it('should update the URL parameter when the parameter ref changes', async () => {
            testee = withSetup(() => useQuery('page', 1)).instance;
            testee.parameter.value = 10;
            await nextTick();
            expect(mockRouterReplace).toHaveBeenCalledTimes(1);
            expect(mockRouterReplace).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: { page: '10' },
                })
            );
            expect(mockRoute.value.query.page).toBe('10');
        });

        it('should remove the URL parameter when the parameter ref is set to the default value', async () => {
            mockRoute.value.query = { page: '5' };
            testee = withSetup(() => useQuery('page', 1)).instance;
            expect(testee.parameter.value).toBe(5);
            mockRouterReplace.mockClear();

            testee.parameter.value = 1;
            await nextTick();
            expect(mockRouterReplace).toHaveBeenCalledTimes(1);
            expect(mockRouterReplace).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: {},
                })
            );
            expect(mockRoute.value.query.page).toBeUndefined();
        });
    });

    describe('boolean parameters', () => {
        it('should load the default value if the parameter is not present in the URL', () => {
            testee = withSetup(() => useQuery('active', false)).instance;
            expect(testee.parameter.value).toBe(false);
        });

        it('should load true from the URL', () => {
            mockRoute.value.query = { active: 'true' };
            testee = withSetup(() => useQuery('active', false)).instance;
            expect(testee.parameter.value).toBe(true);
        });

        it('should load false from the URL', () => {
            mockRoute.value.query = { active: 'false' };
            testee = withSetup(() => useQuery('active', true)).instance; // Default true to ensure false is read
            expect(testee.parameter.value).toBe(false);
        });

        it('should load the default value if the URL parameter is not "true" or "false"', () => {
            mockRoute.value.query = { active: '1' };
            testee = withSetup(() => useQuery('active', false)).instance;
            expect(testee.parameter.value).toBe(false);
        });

        it('should update the URL parameter when the parameter ref changes', async () => {
            testee = withSetup(() => useQuery('active', false)).instance;
            testee.parameter.value = true;
            await nextTick();
            expect(mockRouterReplace).toHaveBeenCalledTimes(1);
            expect(mockRouterReplace).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: { active: 'true' },
                })
            );
            expect(mockRoute.value.query.active).toBe('true');
        });

        it('should remove the URL parameter when the parameter ref is set to the default value', async () => {
            mockRoute.value.query = { active: 'true' };
            testee = withSetup(() => useQuery('active', false)).instance;
            expect(testee.parameter.value).toBe(true);
            mockRouterReplace.mockClear();

            testee.parameter.value = false;
            await nextTick();
            expect(mockRouterReplace).toHaveBeenCalledTimes(1);
            expect(mockRouterReplace).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: {},
                })
            );
            expect(mockRoute.value.query.active).toBeUndefined();
        });
    });

    describe('string array parameters', () => {
        it('should load the default value if the parameter is not present in the URL', () => {
            testee = withSetup(() => useQuery('tags', [])).instance;
            expect(testee.parameter.value).toEqual([]);
        });

        it('should load the value from the URL as an array', () => {
            mockRoute.value.query = { tags: 'tag1,tag2,tag3' };
            testee = withSetup(() => useQuery('tags', [])).instance;
            expect(testee.parameter.value).toEqual(['tag1', 'tag2', 'tag3']);
        });

        it('should load and decode the value from the URL as an array', () => {
            mockRoute.value.query = { tags: encodeURIComponent('tag one,tag two') };
            testee = withSetup(() => useQuery('tags', [])).instance;
            expect(testee.parameter.value).toEqual(['tag one', 'tag two']);
        });

        it('should filter out empty entries in the array', () => {
            mockRoute.value.query = { tags: 'tag1,,tag2, ' };
            testee = withSetup(() => useQuery('tags', [])).instance;
            expect(testee.parameter.value).toEqual(['tag1', 'tag2']);
        });

        it('should update the URL parameter when the parameter ref changes', async () => {
            testee = withSetup(() => useQuery('tags', [])).instance;
            testee.parameter.value = ['new_tag1', 'new_tag2'];
            await nextTick();
            expect(mockRouterReplace).toHaveBeenCalledTimes(1);
            expect(mockRouterReplace).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: { tags: encodeURIComponent('new_tag1,new_tag2') },
                })
            );
            expect(mockRoute.value.query.tags).toBe(encodeURIComponent('new_tag1,new_tag2'));
        });

        it('should remove the URL parameter when the parameter ref is set to the default value', async () => {
            mockRoute.value.query = { tags: 'existing_tag' };
            testee = withSetup(() => useQuery('tags', [])).instance;
            expect(testee.parameter.value).toEqual(['existing_tag']);
            mockRouterReplace.mockClear();

            testee.parameter.value = [];
            await nextTick();
            expect(mockRouterReplace).toHaveBeenCalledTimes(1);
            expect(mockRoute.value.query.tags).toBeUndefined();
        });
    });
});
