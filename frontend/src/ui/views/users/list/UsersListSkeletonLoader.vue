<template>
    <tr v-for="i in props.count || 20" :key="i" class="animate-pulse">
        <td></td>
        <td class="w-1/4">
            <p class="mb-1 h-5 w-64 rounded-lg bg-surface-container-highest"></p>
            <p class="flex items-center space-x-2 text-sm font-light">
                <span class="inline-block h-3 w-16 rounded-lg bg-surface-container-highest"></span>
                <span class="inline-block h-3 w-16 rounded-lg bg-surface-container-highest"></span>
            </p>
        </td>
        <td class="w-1/4">
            <div class="flex gap-2">
                <span class="inline-block h-4 w-24 rounded-lg bg-surface-container-highest"></span>
                <span class="inline-block h-4 w-20 rounded-lg bg-surface-container-highest"></span>
            </div>
        </td>
        <td class="w-1/5">
            <div class="flex gap-4">
                <div v-for="n in 4" :key="n">
                    <p class="mb-1 h-5 w-8 rounded-lg bg-surface-container-highest"></p>
                    <p class="h-3 w-12 rounded-lg bg-surface-container-highest"></p>
                </div>
            </div>
        </td>

        <td class="w-1/12">
            <p class="mb-1 h-5 w-16 rounded-lg bg-surface-container-highest"></p>
            <p class="h-3 w-20 rounded-lg bg-surface-container-highest"></p>
        </td>

        <td class="w-1/12">
            <div class="inline-flex h-8 w-auto items-center space-x-2 rounded-full bg-surface-container-highest py-1 pl-3 pr-4">
                <i class="fa-solid fa-circle text-surface-container-high"></i>
                <p class="h-3 w-20 rounded-lg"></p>
            </div>
        </td>

        <td class="w-0">
            <div class="px-4 py-2">
                <i class="fa-solid fa-circle text-surface-container-high"></i>
            </div>
        </td>
        <td></td>
    </tr>
</template>
<script lang="ts" setup>
interface Props {
    count?: number;
}

const props = defineProps<Props>();
</script>
