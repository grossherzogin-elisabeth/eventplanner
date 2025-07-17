<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-scroll">
        <div v-if="props.backTo" class="absolute left-6 top-7 z-30 hidden xl:block">
            <BackButton :to="props.backTo" />
        </div>
        <div class="sticky top-10 z-20 hidden bg-surface px-8 pb-2 pt-8 md:px-16 lg:block xl:top-0 xl:mx-4">
            <div class="flex items-center xl:max-w-5xl">
                <div class="w-0 flex-grow pr-4">
                    <h1 class="hidden truncate xl:block">
                        <slot name="header" />
                    </h1>
                </div>
                <div class="flex items-stretch space-x-2">
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
        </div>

        <div class="flex-1">
            <slot name="content" />
        </div>

        <div class="h-16 xl:hidden"></div>
        <div
            class="pointer-events-none fixed bottom-0 left-0 right-0 z-10 flex items-stretch justify-end space-x-2 pb-4 pr-3 pt-6 md:pr-14 lg:hidden"
        >
            <div class="details-page-primary-button pointer-events-auto w-auto">
                <slot name="primary-button" />
            </div>
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
