<template>
    <button class="btn-back" @click="back()">
        <i class="fa-solid fa-arrow-left"></i>
    </button>
</template>

<script lang="ts" setup>
import type {
    RouteRecordMultipleViews,
    RouteRecordMultipleViewsWithChildren,
    RouteRecordSingleView,
    RouteRecordSingleViewWithChildren,
} from 'vue-router';
import { useRouter } from 'vue-router';
import { useRouterStack } from '@/ui/composables/RouterStack';

interface Props {
    to:
        | string
        | Partial<RouteRecordSingleView>
        | Partial<RouteRecordSingleViewWithChildren>
        | Partial<RouteRecordMultipleViews>
        | Partial<RouteRecordMultipleViewsWithChildren>;
}

const props = defineProps<Props>();
const routerStack = useRouterStack();
const router = useRouter();

async function back() {
    const backTo = routerStack.getLastOther();
    if (backTo) {
        await router.push(backTo);
    } else {
        await router.push(props.to);
    }
}
</script>
