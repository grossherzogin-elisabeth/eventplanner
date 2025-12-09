<template>
    <div class="flex h-full flex-1 flex-col xl:overflow-y-scroll">
        <div v-if="props.backTo" class="absolute top-7 left-6 z-30 hidden xl:block">
            <BackButton :to="props.backTo" />
        </div>
        <div class="bg-surface sticky top-10 z-20 hidden px-8 pt-8 pb-2 md:px-16 lg:block xl:top-0 xl:mx-4">
            <div class="flex items-center xl:max-w-5xl">
                <div class="w-0 grow pr-4">
                    <h1 class="hidden truncate xl:block" data-test-id="title">
                        <slot name="header" />
                    </h1>
                </div>
                <div class="flex items-stretch space-x-2">
                    <div class="pointer-events-auto w-auto">
                        <slot name="primary-button" />
                    </div>
                    <div class="pointer-events-auto hidden items-stretch space-x-2 lg:flex">
                        <slot name="secondary-buttons" />
                    </div>
                    <PageActionsContextMenu
                        v-if="$slots['actions-menu'] !== undefined"
                        class="pointer-events-auto"
                        data-test-id="actions-menu"
                    >
                        <ul data-test-id="context-menu">
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
            class="pointer-events-none fixed right-0 bottom-0 left-0 z-10 flex items-stretch justify-end space-x-2 pt-6 pr-3 pb-4 md:pr-14 lg:hidden"
        >
            <div class="floating pointer-events-auto w-auto">
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
