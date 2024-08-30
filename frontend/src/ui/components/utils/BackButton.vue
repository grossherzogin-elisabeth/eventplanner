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

interface Props {
    to:
        | string
        | Partial<RouteRecordSingleView>
        | Partial<RouteRecordSingleViewWithChildren>
        | Partial<RouteRecordMultipleViews>
        | Partial<RouteRecordMultipleViewsWithChildren>;
}

const props = defineProps<Props>();

const router = useRouter();

async function back() {
    if (!router.options.history.state.back) {
        router.push(props.to);
    }
    const origin = window.location.origin;
    let previous = new URL(origin + String(router.options.history.state.back));
    let current = new URL(origin + String(router.options.history.state.current));

    const max = 10;
    while (previous.origin === current.origin && previous.pathname === current.pathname) {
        router.back();
        previous = new URL(String(router.options.history.state.back));
        current = new URL(String(router.options.history.state.current));
        if (max < 0) {
            return;
        }
    }
    router.back();
}
</script>
