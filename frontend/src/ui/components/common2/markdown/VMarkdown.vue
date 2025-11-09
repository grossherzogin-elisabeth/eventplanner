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
    font-weight: var(--font-weight-bold);
    color: var(--color-primary);
}

.markdown a:hover {
    text-decoration-line: underline;
}

.markdown p {
    margin-bottom: 1rem;
}

.markdown h1,
.markdown h2,
.markdown h3,
.markdown h4,
.markdown h5,
.markdown h6 {
    margin-top: 1rem;
}

.markdown li {
    list-style-type: disc;
    margin-left: 1rem;
}
</style>
