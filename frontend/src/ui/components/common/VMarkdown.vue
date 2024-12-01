<template>
    <!-- eslint-disable-next-line vue/no-v-html -->
    <div class="markdown" v-html="rendered"></div>
</template>
<script lang="ts" setup>
import { computed } from 'vue';
import Markdown from 'markdown-it';

interface Props {
    value: string;
}

const props = defineProps<Props>();

const md = Markdown();
const rendered = computed<string>(() => {
    const html = md.render(props.value);
    return html.replace('<a href="', '<a target="_blank" href="');
});
</script>

<style>
.markdown a {
    @apply font-bold text-primary hover:underline;
}
</style>
