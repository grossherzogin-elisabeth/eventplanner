<template>
    <div v-if="pageCount > 1" class="pagination">
        <button
            :class="{ enabled: page > 0 }"
            :disabled="page === 0"
            class="pagination-page"
            @click="emit('update:page', page - 1)"
        >
            <i class="fa-solid fa-chevron-left text-xs"></i>
        </button>

        <template v-for="p in visiblePages" :key="`${p}-group`">
            <button
                v-if="typeof p === 'number'"
                :class="{ active: p === page }"
                class="pagination-page enabled"
                @click="emit('update:page', p)"
            >
                <span>{{ p + 1 }}</span>
            </button>
            <span v-else class="pagination-page"> ... </span>
        </template>

        <button
            :class="{ enabled: page < pageCount - 1 }"
            :disabled="page === pageCount - 1"
            class="pagination-page"
            @click="emit('update:page', page + 1)"
        >
            <i class="fa-solid fa-chevron-right text-xs"></i>
        </button>
    </div>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';

interface Props {
    page: number;
    count: number;
    pageSize?: number;
}

type Emits = (e: 'update:page', value: number) => void;

/**
 * --------------------------------------------------------------------------------------------------------
 * Component Definition
 * --------------------------------------------------------------------------------------------------------
 */

const props = defineProps<Props>();

const emit = defineEmits<Emits>();

const visiblePagesCount = ref<number>(8);
const pageSize = computed<number>(() => props.pageSize || 10);
const pageCount = computed<number>(() => Math.ceil(props.count / pageSize.value));

const visiblePages = computed<(number | string)[]>(() => {
    const pages: (number | string)[] = [0];
    const lastPageIndex = pageCount.value - 1;
    // dynamic pages in the middle, first and last slot are reserved
    const centerPages = visiblePagesCount.value - 2;
    let lowerBound = Math.max(props.page - Math.floor(centerPages / 2), 1);
    let upperBound = lowerBound + centerPages;
    if (upperBound > lastPageIndex) {
        const shift = upperBound - lastPageIndex;
        upperBound -= shift;
        lowerBound = Math.max(lowerBound - shift, 1);
    }
    for (let i = lowerBound; i < upperBound; i++) {
        pages.push(i);
    }
    pages.push(lastPageIndex);

    if (pages[1] !== 1) {
        pages[1] = '...';
    }
    if (pages[pages.length - 2] !== lastPageIndex - 1) {
        pages[pages.length - 2] = '...';
    }
    return pages;
});
</script>
