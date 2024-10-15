<template>
    <div :class="$attrs.class + ' ' + classes.join(' ')">
        <table class="v-table">
            <thead>
                <tr ref="head">
                    <th></th>
                    <slot name="head" :sort-by="sortCol" :sort-direction="sortDir > 0 ? 'asc' : 'desc'"></slot>
                    <th></th>
                </tr>
            </thead>
            <!-- loading -->
            <tbody v-if="loading">
                <slot name="loading" :colspan="columnCount">
                    <tr>
                        <td></td>
                        <td :colspan="columnCount" class="italic">Wird geladen...</td>
                        <td></td>
                    </tr>
                </slot>
            </tbody>
            <!-- no data -->
            <tbody v-else-if="empty">
                <tr class="no-data">
                    <td></td>
                    <slot name="no-data" :colspan="columnCount">
                        <td :colspan="columnCount" class="italic">Keine Daten.</td>
                    </slot>
                    <td></td>
                </tr>
            </tbody>
            <!-- data -->
            <tbody v-else>
                <tr v-for="(row, index) in pagedItems" :key="index" @click="emit('click', row)">
                    <td></td>
                    <slot name="row" :item="row" :index="index">
                        <td v-for="(val, colIndex) in Object.values(row)" :key="colIndex">
                            {{ val }}
                        </td>
                    </slot>
                    <td></td>
                </tr>
            </tbody>
        </table>
    </div>
    <VPagination v-if="usePagination" v-model:page="page" :count="sortedItems.length" :page-size="pageSize" />
</template>

<script setup generic="T extends {}" lang="ts">
import { computed, ref, useSlots, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ObjectUtils } from '@/common';
import { useQueryStateSync } from '@/ui/composables/QueryState';
import VPagination from './VPagination.vue';

/**
 * --------------------------------------------------------------------------------------------------------
 * Usage Example
 * --------------------------------------------------------------------------------------------------------
 *
 * ````
 * <VTable :items="languages" @click="editLanguage" class="interactive" query>
 *     <template #head>
 *         <th data-sortby="key">Key</th>
 *         <th data-sortby="name.de">Name</th>
 *         <th data-sortby="hidden">Aktiv</th>
 *         <th>Aktionen</th>
 *     </template>
 *     <template #row="{ item }">
 *         <td class="w-1/5">{{ item.key }}</td>
 *         <td class="w-4/5">{{ item.name.de }}</td>
 *         <td>
 *             <IconCheck v-if="!item.hidden" class="w-4 h-4" />
 *         </td>
 *         <td class="text-right">
 *             <div class="flex items-center justify-end space-x-4">
 *                 <button @click.stop="editLanguage(item)">
 *                     <IconEdit class="w-4 h-4 text-blue-dark stroke-1" />
 *                 </button>
 *                 <button @click.stop="deleteLanguage(item)">
 *                     <IconDelete class="w-4 h-4 text-red-dark stroke-1" />
 *                 </button>
 *             </div>
 *         </td>
 *     </template>
 * </VTable>
 * ````
 */

interface Props<T> {
    /**
     * The items to display in this table
     */
    items?: T[];
    /**
     * Pagination page size, set to -1 to disable pagination
     */
    pageSize?: number;
    /**
     * Current page, can be synced with `v-model:page`
     */
    page?: number;
    /**
     * Should page and sorting be saved in the url query?
     * If true the query parameters `page`, `sort` and `direction` will be set
     */
    query?: boolean;
    /**
     * Default sort column
     */
    sortBy?: string;
    /**
     * Default sort direction
     */
    sortDirection?: string;
}

interface Emits {
    (e: 'click', value: T): void;

    (e: 'update:page', value: number): void;
}

interface Slots {
    'head'?: (props: { sortBy: string; sortDirection: 'asc' | 'desc' }) => void;
    'row'?: (props: { item: T; index: number }) => void;
    'no-data'?: (props: { colspan: number }) => void;
    'loading'?: (props: { colspan: number }) => void;
}

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const slots = useSlots();
const router = useRouter();

const props = defineProps<Props<T>>();
const emit = defineEmits<Emits>();
defineSlots<Slots>();

const usePagination = computed<boolean>(() => props.pageSize !== -1);
const usePageSize = computed<number>(() => props.pageSize || 10);

const head = ref<HTMLTableRowElement | null>(null);
const page = ref<number>(props.page || 0);
const sortCol = ref<string>('');
const sortDir = ref<number>(1);
const loading = computed<boolean>(() => props.items === undefined);
const empty = computed<boolean>(() => props.items !== undefined && props.items.length === 0);

useQueryStateSync<number>(
    'page',
    () => page.value,
    (v) => (page.value = v)
);
useQueryStateSync<string>(
    'sort',
    () => sortCol.value,
    (v) => (sortCol.value = v)
);
useQueryStateSync<number>(
    'direction',
    () => sortDir.value,
    (v) => (sortDir.value = v)
);

const classes = computed<string[]>(() => {
    const result: string[] = [];
    if (loading.value) {
        result.push('loading');
    }
    return result;
});
const sortedItems = computed<T[]>(() => {
    if (!props.items) {
        return [];
    }
    if (sortCol.value === '') {
        return props.items;
    }
    return ObjectUtils.deepCopy(props.items).sort((a, b) => {
        const va = ObjectUtils.extractValue(a, sortCol.value);
        const vb = ObjectUtils.extractValue(b, sortCol.value);

        // Compare numbers
        if (typeof va === 'number' && typeof vb === 'number') {
            return va - vb * sortDir.value;
        }

        // Compare dates
        const vaDate = new Date(va);
        const vbDate = new Date(vb);
        if (
            !vaDate.toString().toLowerCase().includes('invalid') &&
            !vbDate.toString().toLowerCase().includes('invalid')
        ) {
            return (vaDate.getTime() - vbDate.getTime()) * sortDir.value;
        }

        return String(va).localeCompare(String(vb)) * sortDir.value;
    });
});
const pagedItems = computed<T[]>(() => {
    if (sortedItems.value.length <= usePageSize.value || !usePagination.value) {
        return sortedItems.value;
    } else {
        const start = page.value * usePageSize.value;
        const end = start + usePageSize.value;
        return sortedItems.value.slice(start, end);
    }
});
const columnCount = computed<number>(() => {
    if (slots.head) {
        return slots.head().length;
    }
    if (pagedItems.value[0]) {
        return Object.keys(pagedItems.value[0]).length;
    }
    return 0;
});

function sort(th: HTMLTableCellElement): void {
    // remove all sort classes from all th elements in the header
    if (th.parentElement) {
        th.parentElement
            .querySelectorAll<HTMLTableCellElement>('th')
            .forEach((sibling) => sibling.classList.remove('sort', 'asc', 'desc'));
    }
    const thSortBy = th.dataset.sortby || '';
    if (sortCol.value === thSortBy) {
        sortDir.value = sortDir.value * -1;
        page.value = 0;
    } else {
        sortCol.value = thSortBy;
        sortDir.value = 1;
        page.value = 0;
    }

    setSortingIndicators();
}

function registerSortListeners(): void {
    if (head.value) {
        const headers = head.value.querySelectorAll<HTMLTableCellElement>('th[data-sortby]');
        headers.forEach((th) => {
            th.removeEventListener('click', () => sort(th));
        });
        headers.forEach((th) => {
            th.addEventListener('click', () => sort(th));
        });
    }
}

function setSortingIndicators(): void {
    if (head.value && sortCol.value) {
        const th = head.value.querySelector(`th[data-sortby=${sortCol.value}]`);
        if (th) {
            th.classList.add('sort');
            th.classList.add(sortDir.value === 1 ? 'asc' : 'desc');
        }
    }
}

async function init(): Promise<void> {
    registerSortListeners();
    watch(() => slots.head, registerSortListeners);
    watch(head, registerSortListeners);
    watch(page, () => emit('update:page', page.value));

    await router.isReady();
    setSortingIndicators();
}

init();
</script>
