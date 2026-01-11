import type { Ref } from 'vue';
import { nextTick } from 'vue';
import type { Router } from 'vue-router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useQuery } from '@/ui/composables/QueryState';
import { mockRouter, mockedCurrentRoute, mockedReplaceFunc } from '~/mocks/router/mockRouter';

const router = mockRouter();
vi.mock('vue-router', () => ({
    useRouter: (): Partial<Router> => router,
}));

describe('useQuery', () => {
    let testee: { parameter: Ref; clearWatchers: () => void };

    beforeEach(() => {
        router.replace({ name: 'test-route' });
        vi.clearAllMocks();
    });

    afterEach(() => {
        testee.clearWatchers();
    });

    describe('string parameters', () => {
        it('should load the default value if the parameter is not present in the URL', () => {
            testee = useQuery('search', 'default');
            expect(testee.parameter.value).toBe('default');
            expect(mockedReplaceFunc).not.toHaveBeenCalled();
        });

        it('should load the value from the URL if the parameter is present', () => {
            mockedCurrentRoute.value.query = { search: 'hello' };
            testee = useQuery('search', 'default');
            expect(testee.parameter.value).toBe('hello');
            expect(mockedReplaceFunc).not.toHaveBeenCalled();
        });

        it('should load and decode the value from the URL', () => {
            mockedCurrentRoute.value.query = { search: encodeURIComponent('hello world') };
            testee = useQuery('search', 'default');
            expect(testee.parameter.value).toBe('hello world');
        });

        it('should update the URL parameter when the parameter ref changes', async () => {
            testee = useQuery('search', 'default');
            testee.parameter.value = 'new_value';
            await nextTick();
            expect(mockedReplaceFunc).toHaveBeenCalledTimes(1);
            expect(mockedReplaceFunc).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: { search: 'new_value' },
                })
            );
            expect(mockedCurrentRoute.value.query.search).toBe('new_value');
        });

        it('should remove the URL parameter when the parameter ref is set to the default value', async () => {
            mockedCurrentRoute.value.query = { search: 'existing_value' };
            testee = useQuery('search', 'default');
            expect(testee.parameter.value).toBe('existing_value');
            testee.parameter.value = 'default';
            await nextTick();
            expect(mockedReplaceFunc).toHaveBeenCalledTimes(1);
            expect(mockedReplaceFunc).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: {},
                })
            );
            expect(mockedCurrentRoute.value.query.search).toBeUndefined();
        });

        it('should remove the URL parameter when the parameter ref is set to undefined', async () => {
            mockedCurrentRoute.value.query = { search: 'existing_value' };
            testee = useQuery<string | undefined>('search', 'default');
            expect(testee.parameter.value).toBe('existing_value');
            testee.parameter.value = undefined;
            await nextTick();
            expect(mockedCurrentRoute.value.query.search).toBeUndefined();
        });

        it('should remove the URL parameter when the parameter ref is set to null', async () => {
            mockedCurrentRoute.value.query = { search: 'existing_value' };
            testee = useQuery<string | null>('search', 'default');
            expect(testee.parameter.value).toBe('existing_value');
            testee.parameter.value = null;
            await nextTick();
            expect(mockedCurrentRoute.value.query.search).toBeUndefined();
        });

        it('should update the parameter ref when the URL parameter changes', async () => {
            testee = useQuery('search', 'default');
            expect(testee.parameter.value).toBe('default');

            mockedCurrentRoute.value = {
                ...mockedCurrentRoute.value,
                query: { search: 'from_url' },
            };
            await nextTick();
            expect(testee.parameter.value).toBe('from_url');
        });

        it('should not call router.replace if the parameter ref is set to default and the URL parameter is already absent', async () => {
            mockedCurrentRoute.value.query = {}; // No 'search' parameter
            testee = useQuery('search', 'default');
            expect(testee.parameter.value).toBe('default'); // initial load sets to default
            mockedReplaceFunc.mockClear(); // clear any potential initial calls

            testee.parameter.value = 'default'; // setting to default again
            await nextTick();
            expect(mockedReplaceFunc).not.toHaveBeenCalled();
        });
    });

    describe('numeric parameters', () => {
        it('should load the default value if the parameter is not present in the URL', () => {
            testee = useQuery('page', 1);
            expect(testee.parameter.value).toBe(1);
        });

        it('should load the value from the URL if the parameter is present', () => {
            mockedCurrentRoute.value.query = { page: '5' };
            testee = useQuery('page', 1);
            expect(testee.parameter.value).toBe(5);
        });

        it('should load the default value if the URL parameter is not a valid number', () => {
            mockedCurrentRoute.value.query = { page: 'abc' };
            testee = useQuery('page', 1);
            expect(testee.parameter.value).toBe(1);
        });

        it('should update the URL parameter when the parameter ref changes', async () => {
            testee = useQuery('page', 1);
            testee.parameter.value = 10;
            await nextTick();
            expect(mockedReplaceFunc).toHaveBeenCalledTimes(1);
            expect(mockedReplaceFunc).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: { page: '10' },
                })
            );
            expect(mockedCurrentRoute.value.query.page).toBe('10');
        });

        it('should remove the URL parameter when the parameter ref is set to the default value', async () => {
            mockedCurrentRoute.value.query = { page: '5' };
            testee = useQuery('page', 1);
            expect(testee.parameter.value).toBe(5);
            mockedReplaceFunc.mockClear();

            testee.parameter.value = 1;
            await nextTick();
            expect(mockedReplaceFunc).toHaveBeenCalledTimes(1);
            expect(mockedReplaceFunc).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: {},
                })
            );
            expect(mockedCurrentRoute.value.query.page).toBeUndefined();
        });
    });

    describe('boolean parameters', () => {
        it('should load the default value if the parameter is not present in the URL', () => {
            testee = useQuery('active', false);
            expect(testee.parameter.value).toBe(false);
        });

        it('should load true from the URL', () => {
            mockedCurrentRoute.value.query = { active: 'true' };
            testee = useQuery('active', false);
            expect(testee.parameter.value).toBe(true);
        });

        it('should load false from the URL', () => {
            mockedCurrentRoute.value.query = { active: 'false' };
            testee = useQuery('active', true); // Default true to ensure false is read
            expect(testee.parameter.value).toBe(false);
        });

        it('should load the default value if the URL parameter is not "true" or "false"', () => {
            mockedCurrentRoute.value.query = { active: '1' };
            testee = useQuery('active', false);
            expect(testee.parameter.value).toBe(false);
        });

        it('should update the URL parameter when the parameter ref changes', async () => {
            testee = useQuery('active', false);
            testee.parameter.value = true;
            await nextTick();
            expect(mockedReplaceFunc).toHaveBeenCalledTimes(1);
            expect(mockedReplaceFunc).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: { active: 'true' },
                })
            );
            expect(mockedCurrentRoute.value.query.active).toBe('true');
        });

        it('should remove the URL parameter when the parameter ref is set to the default value', async () => {
            mockedCurrentRoute.value.query = { active: 'true' };
            testee = useQuery('active', false);
            expect(testee.parameter.value).toBe(true);
            mockedReplaceFunc.mockClear();

            testee.parameter.value = false;
            await nextTick();
            expect(mockedReplaceFunc).toHaveBeenCalledTimes(1);
            expect(mockedReplaceFunc).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: {},
                })
            );
            expect(mockedCurrentRoute.value.query.active).toBeUndefined();
        });
    });

    describe('string array parameters', () => {
        it('should load the default value if the parameter is not present in the URL', () => {
            testee = useQuery('tags', []);
            expect(testee.parameter.value).toEqual([]);
        });

        it('should load the value from the URL as an array', () => {
            mockedCurrentRoute.value.query = { tags: 'tag1,tag2,tag3' };
            testee = useQuery('tags', []);
            expect(testee.parameter.value).toEqual(['tag1', 'tag2', 'tag3']);
        });

        it('should load and decode the value from the URL as an array', () => {
            mockedCurrentRoute.value.query = { tags: encodeURIComponent('tag one,tag two') };
            testee = useQuery('tags', []);
            expect(testee.parameter.value).toEqual(['tag one', 'tag two']);
        });

        it('should filter out empty entries in the array', () => {
            mockedCurrentRoute.value.query = { tags: 'tag1,,tag2, ' };
            testee = useQuery('tags', []);
            expect(testee.parameter.value).toEqual(['tag1', 'tag2']);
        });

        it('should update the URL parameter when the parameter ref changes', async () => {
            testee = useQuery('tags', []);
            testee.parameter.value = ['new_tag1', 'new_tag2'];
            await nextTick();
            expect(mockedReplaceFunc).toHaveBeenCalledTimes(1);
            expect(mockedReplaceFunc).toHaveBeenCalledWith(
                expect.objectContaining({
                    query: { tags: encodeURIComponent('new_tag1,new_tag2') },
                })
            );
            expect(mockedCurrentRoute.value.query.tags).toBe(encodeURIComponent('new_tag1,new_tag2'));
        });

        it('should remove the URL parameter when the parameter ref is set to the default value', async () => {
            mockedCurrentRoute.value.query = { tags: 'existing_tag' };
            testee = useQuery('tags', []);
            expect(testee.parameter.value).toEqual(['existing_tag']);
            mockedReplaceFunc.mockClear();

            testee.parameter.value = [];
            await nextTick();
            expect(mockedReplaceFunc).toHaveBeenCalledTimes(1);
            expect(mockedCurrentRoute.value.query.tags).toBeUndefined();
        });
    });
});
