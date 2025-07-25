<template>
    <div class="mb-4" :class="$attrs.class + ' ' + classes.join(' ')">
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
            <component
                :is="props.sortable ? VueDraggableNext : 'tbody'"
                tag="tbody"
                :list="props.sortable ? pagedItems : undefined"
                handle=".cursor-move"
                @change="onReorder"
            >
                <tr
                    v-for="(row, index) in pagedItems"
                    ref="rows"
                    :key="index"
                    :class="{ selected: row.selected }"
                    @touchstart="touch.start($event).then(() => onLongTouch(row as T & Selectable, index))"
                    @touchend="touch.end()"
                    @touchmove="touch.update($event)"
                    @click.stop.prevent="onClick($event, row as T & Selectable)"
                >
                    <td></td>
                    <td v-if="props.sortable && viewPortSize.width.value > 1024" class="w-0">
                        <i class="fa-solid fa-grip-vertical cursor-move text-secondary-variant"></i>
                    </td>
                    <td
                        v-if="props.multiselection && (selected.length > 0 || viewPortSize.width.value > 1024)"
                        @click.stop.prevent="row.selected = !row.selected"
                    >
                        <div v-if="row.selected">
                            <i class="fa-solid fa-check-square text-xl text-primary sm:text-2xl"></i>
                        </div>
                        <div v-else>
                            <i class="fa-solid fa-square text-xl text-surface-container-high sm:text-2xl"></i>
                        </div>
                    </td>
                    <slot
                        name="row"
                        :item="row as T & Selectable"
                        :index="index"
                        :first="index === 0"
                        :last="index === pagedItems.length - 1"
                    >
                        <td v-for="(val, colIndex) in Object.values(row)" :key="colIndex">
                            {{ val }}
                        </td>
                    </slot>
                    <td
                        v-if="$slots['context-menu']"
                        ref="contextColumns"
                        class="w-0"
                        @click.stop="openContextMenu(contextColumns?.[index], row as T & Selectable)"
                    >
                        <button class="icon-button">
                            <i class="fa-solid fa-ellipsis-vertical" />
                        </button>
                    </td>
                    <td></td>
                </tr>
            </component>
        </table>
        <VDropdownWrapper
            v-if="dropdownAnchor && dropdownItem"
            :anchor="dropdownAnchor"
            anchor-align-x="right"
            anchor-align-y="top"
            dropdown-position-x="left"
            dropdown-position-y="bottom"
            min-width="20rem"
            max-width="20rem"
            @close="dropdownAnchor = null"
        >
            <div class="mt-2 rounded-xl bg-surface-container-high p-4 shadow-xl">
                <ul>
                    <template v-if="props.multiselection">
                        <li v-if="dropdownItem.selected" class="context-menu-item" @click="dropdownItem.selected = !dropdownItem.selected">
                            <i class="fa-solid fa-xmark" />
                            <span>Abwählen</span>
                        </li>
                        <li v-else class="context-menu-item" @click="dropdownItem.selected = !dropdownItem.selected">
                            <i class="fa-solid fa-check" />
                            <span>Auswählen</span>
                        </li>
                    </template>
                    <slot name="context-menu" :item="dropdownItem" />
                </ul>
            </div>
        </VDropdownWrapper>
    </div>
    <VPagination v-if="usePagination" v-model:page="page" :count="sortedItems.length" :page-size="pageSize" />
</template>

<script setup generic="T extends {}" lang="ts">
import type { ComputedRef, Ref } from 'vue';
import { computed, ref, useSlots, watch } from 'vue';
import { useRouter } from 'vue-router';
import { deepCopy, extractValue } from '@/common';
import { useLongTouch } from '@/ui/components/common/table/touch.ts';
import { useQueryStateSync } from '@/ui/composables/QueryState';
import { useViewportSize } from '@/ui/composables/ViewportSize.ts';
import type { Selectable } from '@/ui/model/Selectable.ts';
import { VueDraggableNext } from 'vue-draggable-next';
import VDropdownWrapper from '../dropdown/VDropdownWrapper.vue';
import VPagination from './VPagination.vue';

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
    /**
     * Should the table offer multi selection? items should be of type Selectable in that case.
     */
    multiselection?: boolean;

    sortable?: boolean;
}

interface Emits {
    (e: 'click', param: { item: T; event: MouseEvent }): void;
    (e: 'reordered'): void;
    (e: 'update:page', value: number): void;
}

interface Slots {
    'head'?: (props: { sortBy: string; sortDirection: 'asc' | 'desc' }) => void;
    'row'?: (props: { item: T & Selectable; index: number; first: boolean; last: boolean }) => void;
    'no-data'?: (props: { colspan: number }) => void;
    'context-menu'?: (props: { item: T & Selectable }) => void;
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

const viewPortSize = useViewportSize();
const touch = useLongTouch();

const usePagination: ComputedRef<boolean> = computed(() => props.pageSize !== -1);
const usePageSize: ComputedRef<number> = computed(() => props.pageSize || 10);

const head: Ref<HTMLTableRowElement | null> = ref(null);
const dropdownAnchor: Ref<HTMLElement | null> = ref(null);
const dropdownItem: Ref<(T & Selectable) | null> = ref(null);
const contextColumns: Ref<HTMLTableCellElement[] | null> = ref(null);
const rows: Ref<HTMLTableRowElement[] | null> = ref(null);
const page: Ref<number> = ref(props.page || 0);
const sortCol: Ref<string> = ref('');
const sortDir: Ref<number> = ref(1);
const loading: ComputedRef<boolean> = computed(() => props.items === undefined);
const empty: ComputedRef<boolean> = computed(() => props.items !== undefined && props.items.length === 0);

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
    if (props.multiselection) {
        result.push('with-multi-selection');
    }
    if (slots.contextMenu) {
        result.push('with-context-menu');
    }
    return result;
});

const selected = computed<(T & Selectable)[]>(() => {
    return pagedItems.value.filter((it) => it.selected);
});

const sortedItems = computed<(T & Selectable)[]>(() => {
    if (!props.items) {
        return [];
    }
    if (sortCol.value === '') {
        return props.items;
    }
    return deepCopy(props.items).sort((a, b) => {
        const va = extractValue(a, sortCol.value);
        const vb = extractValue(b, sortCol.value);

        // Compare numbers
        if (typeof va === 'number' && typeof vb === 'number') {
            return va - vb * sortDir.value;
        }

        // Compare dates
        const vaDate = new Date(va);
        const vbDate = new Date(vb);
        if (!vaDate.toString().toLowerCase().includes('invalid') && !vbDate.toString().toLowerCase().includes('invalid')) {
            return (vaDate.getTime() - vbDate.getTime()) * sortDir.value;
        }

        return String(va).localeCompare(String(vb)) * sortDir.value;
    });
});

const pagedItems = computed<(T & Selectable)[]>(() => {
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
        th.parentElement.querySelectorAll<HTMLTableCellElement>('th').forEach((sibling) => sibling.classList.remove('sort', 'asc', 'desc'));
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

function onLongTouch(row: T & Selectable, index: number): void {
    if (props.multiselection) {
        row.selected = !row.selected;
    } else {
        openContextMenu(rows.value?.[index], row);
    }
}

function onClick(event: MouseEvent, row: T & Selectable): void {
    if (touch.isLongTouch()) {
        return;
    }
    if (touch.isTouch() && selected.value.length > 0) {
        row.selected = !row.selected;
        return;
    }
    event.stopPropagation();
    if (props.multiselection) {
        if (event.shiftKey || selected.value.length > 0) {
            event.preventDefault();
            event.stopPropagation();
            row.selected = !row.selected;
            return;
        }
    }
    emit('click', { item: row, event: event });
}

function onReorder(): void {
    emit('reordered');
}

function openContextMenu(anchor: EventTarget | HTMLElement | undefined, row: T & Selectable): void {
    if (slots['context-menu'] && anchor instanceof HTMLElement) {
        dropdownAnchor.value = anchor;
        dropdownItem.value = row;
    }
}

async function init(): Promise<void> {
    registerSortListeners();
    watch(() => slots.head, registerSortListeners);
    watch(head, registerSortListeners);
    watch(page, () => emit('update:page', page.value));

    await router.isReady();
    watch(
        () => props.items,
        (newValue, oldValue) => {
            if (oldValue && newValue && oldValue.length !== newValue.length) {
                // if old and new item count both exist, but are different, then probably the content to display
                // changed a lot, so we should start at page 0 again
                // also prevents displaying a page that does not exist
                page.value = 0;
            }
        }
    );
    setSortingIndicators();
}

init();
</script>
