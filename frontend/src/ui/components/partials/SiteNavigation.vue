<template>
    <div class="flex flex-col border-l">
        <RouterLink
            v-for="siteLink in siteLinks"
            :key="siteLink.hash"
            :to="siteLink.hash"
            class="-ml-px border-l-2 py-4 pl-4"
            :class="currentHash === siteLink.hash ? 'border-primary text-primary' : 'border-transparent'"
        >
            {{ siteLink.name }}
        </RouterLink>
    </div>
</template>
<script lang="ts" setup>
import { nextTick, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

interface SiteLink {
    id: string;
    hash: string;
    name: string;
}

type Emits = (e: 'update:hasItems', value: boolean) => void;

const emit = defineEmits<Emits>();
defineExpose({
    updateSiteLinks: updateSiteLinks,
});

const route = useRoute();

const siteLinks = ref<SiteLink[]>([]);
const visibleAnchors = ref<HTMLElement[]>([]);
const currentHash = ref<string>(route.hash);
const blockScrollObserver = ref<boolean>(false);
const scrollObserver = new IntersectionObserver(updateHash, {
    root: null,
    threshold: 1,
    rootMargin: '50px 0px -200px 0px',
});

function updateSiteLinks(): void {
    siteLinks.value = [];
    scrollObserver.takeRecords().forEach((it) => scrollObserver.unobserve(it.target));
    const elements = document.querySelectorAll('.site-link');
    let anySiteLinkFound = false;
    elements.forEach((element) => {
        if (element.textContent) {
            scrollObserver.observe(element);
            siteLinks.value.push({
                id: element.id,
                hash: `#${element.id}`,
                name: element.textContent,
            });
            anySiteLinkFound = true;
        }
    });
    emit('update:hasItems', anySiteLinkFound);
}

function updateHash(entries: IntersectionObserverEntry[]): void {
    entries
        .filter((it) => it.target.id)
        .forEach((entry) => {
            if (entry.isIntersecting) {
                visibleAnchors.value.push(entry.target as HTMLElement);
            } else {
                visibleAnchors.value = visibleAnchors.value.filter((it) => it.id !== entry.target.id);
            }
            visibleAnchors.value.sort((a, b) => a.scrollTop - b.scrollTop);
            // console.log(visibleAnchors.value.map((it) => it.textContent));
            if (!blockScrollObserver.value) {
                const visibleHashes = visibleAnchors.value.map((it) => `#${it.id}`);
                if (visibleHashes.length > 0 && !visibleHashes.includes(currentHash.value)) {
                    currentHash.value = visibleHashes[0];
                }
            }
        });
}

function updateHashFromRouteChange(): void {
    blockScrollObserver.value = true;
    currentHash.value = route.hash;
    setTimeout(() => (blockScrollObserver.value = false), 1500);
}

function init(): void {
    nextTick(updateSiteLinks);
    watch(
        () => route.path,
        () => nextTick(updateSiteLinks)
    );
    watch(
        () => route.hash,
        () => updateHashFromRouteChange()
    );
}

init();
</script>
