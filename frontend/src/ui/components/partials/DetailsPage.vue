<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-scroll">
        <div v-if="props.backTo" class="absolute left-6 top-6 z-30 hidden xl:block">
            <BackButton :to="props.backTo" />
        </div>
        <div v-if="$slots.header" class="ml-8 bg-surface">
            <div class="z-10 pr-8 md:pl-8 md:pr-16 xl:pl-12 xl:pr-20">
                <slot name="header" />
            </div>
        </div>

        <div class="flex-1">
            <slot name="content" />
        </div>

        <div class="h-16 xl:hidden"></div>
        <div
            class="pointer-events-none fixed bottom-0 left-0 right-0 z-10 flex items-stretch justify-end space-x-2 pb-4 pr-3 pt-6 md:pr-14 lg:pointer-events-auto lg:justify-start lg:border-t lg:border-outline-variant lg:bg-surface lg:px-16 lg:pb-8 xl:sticky xl:px-20"
        >
            <div class="details-page-primary-button pointer-events-auto w-auto">
                <slot name="primary-button" />
            </div>
            <div class="pointer-events-auto hidden items-stretch space-x-2 lg:flex">
                <slot name="secondary-buttons" />
            </div>
            <PageActionsContextMenu v-if="$slots['actions-menu'] !== undefined" class="pointer-events-auto">
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
    backTo?:
        | string
        | Partial<RouteRecordSingleView>
        | Partial<RouteRecordSingleViewWithChildren>
        | Partial<RouteRecordMultipleViews>
        | Partial<RouteRecordMultipleViewsWithChildren>;
}

const props = defineProps<Props>();
</script>
