<template>
    <button class="icon-button" @click="back()">
        <i class="fa-solid fa-arrow-left"></i>
    </button>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router';

const router = useRouter();

async function back(): Promise<void> {
    router.back();
}
</script>
