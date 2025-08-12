<template>
    <div class="h-full overflow-y-auto px-4 pb-8 pt-8 xs:px-8 md:px-16 xl:px-20">
        <div class="wiki max-w-2xl">
            <div v-for="(page, index) in pages" :key="index" class="mb-8">
                <h2 class="mb-4 text-lg font-bold">{{ page.title }}</h2>
                <div v-html="markdown.render(page.content)"></div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import Markdown from 'markdown-it';

const pages = ref<any[]>([]);

const markdown = new Markdown();

function init(): void {
    fetchWiki();
}

async function fetchWiki(): Promise<void> {
    const response = await fetch('/items/Wiki');
    pages.value = (await response.clone().json()).data;
    console.log(pages.value);
}

init();
</script>

<style>
.wiki p {
    @apply mb-4 text-justify;
}

.wiki strike {
    @apply font-black;
}
</style>
