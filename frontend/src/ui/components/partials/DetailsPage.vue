<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-scroll">
        <div v-if="props.backTo" class="absolute left-6 top-6 z-30 hidden xl:block">
            <BackButton :to="props.backTo" />
        </div>
        <div class="bg-primary-50">
            <div class="z-10 px-8 pt-8 md:px-16 xl:px-20">
                <slot name="header" />
            </div>
        </div>

        <div class="flex-1">
            <slot name="content" />
        </div>

        <div
            class="sticky bottom-0 z-10 flex items-stretch justify-end space-x-2 pb-4 pr-3 pt-6 md:pr-14 lg:justify-start lg:border-t lg:border-gray-300 lg:bg-primary-50 lg:px-16 lg:pb-8 xl:px-20"
        >
            <div class="details-page-primary-button w-auto">
                <slot name="primary-button" />
            </div>
            <div class="hidden items-stretch space-x-2 lg:flex">
                <slot name="secondary-buttons" />
            </div>
            <PageActionsContextMenu v-if="$slots['actions-menu'] !== undefined">
                <ul>
                    <slot name="actions-menu" />
                </ul>
            </PageActionsContextMenu>
        </div>
    </div>
</template>
<script lang="ts" setup>
import type {
    RouteRecordMultipleViews,
    RouteRecordMultipleViewsWithChildren,
    RouteRecordSingleView,
    RouteRecordSingleViewWithChildren,
} from 'vue-router';
import PageActionsContextMenu from '@/ui/components/partials/PageActionsContextMenu.vue';
import BackButton from '@/ui/components/utils/BackButton.vue';

interface Props {
    backTo:
        | string
        | Partial<RouteRecordSingleView>
        | Partial<RouteRecordSingleViewWithChildren>
        | Partial<RouteRecordMultipleViews>
        | Partial<RouteRecordMultipleViewsWithChildren>;
}

const props = defineProps<Props>();
</script>

<style>
@media (max-width: 1023px) {
    .details-page-primary-button .btn-primary,
    .details-page-primary-button .btn-secondary,
    .details-page-primary-button .btn-danger {
        @apply px-4 py-4;
        @apply rounded-xl shadow-xl;
        @apply bg-opacity-100 !important;
    }
}
</style>
