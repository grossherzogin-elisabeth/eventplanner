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
    @apply font-bold;
    @apply text-primary;
}

.markdown a:hover {
    text-decoration-line: underline;
}

.markdown p {
    @apply mb-4;
}

.markdown h1,
.markdown h2,
.markdown h3,
.markdown h4,
.markdown h5,
.markdown h6 {
    @apply mt-4;
}

.markdown li {
    list-style-type: disc;
    @apply ml-4;
}
</style>
