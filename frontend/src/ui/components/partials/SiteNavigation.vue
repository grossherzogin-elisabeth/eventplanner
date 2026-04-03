<template>
    <div class="flex flex-col">
        <RouterLink
            v-for="link in links"
            :key="link.hash"
            :to="link.to ?? route"
            class="hover:text-onsurface hover:bg-surface-container truncate rounded-xl px-8 py-2"
            :class="currentHash === link.hash ? 'text-onsurface' : 'text-onsurface/50'"
            @click="scrollToId(link.id)"
        >
            {{ link.name }}
        </RouterLink>
    </div>
</template>
<script lang="ts" setup>
import { computed, nextTick, ref, watch } from 'vue';
import type { RouteLocationRaw } from 'vue-router';
import { useRoute } from 'vue-router';

interface SiteLink {
    id: string;
    hash: string;
    to?: RouteLocationRaw;
    name: string;
}

type Emits = (e: 'update:hasItems', value: boolean) => void;

const emit = defineEmits<Emits>();
defineExpose({
    updateSiteLinks: updateSiteLinks,
});

const route = useRoute();

const menuItems = ref<SiteLink[]>([]);
const visibleAnchors = ref<HTMLElement[]>([]);
const currentHash = ref<string>(route.hash);
const blockScrollObserver = ref<boolean>(false);
const scrollObserver = new IntersectionObserver(updateSelectedFromScroll, {
    root: null,
    threshold: 1,
    rootMargin: '50px 0px -200px 0px',
});

const links = computed<SiteLink[]>(() => {
    return menuItems.value.map((siteLink) => ({
        ...siteLink,
        to: {
            name: route.name,
            query: route.query,
            hash: siteLink.hash,
            params: route.params,
            replace: true,
        },
    }));
});

function updateSiteLinks(): void {
    menuItems.value = [];
    scrollObserver.takeRecords().forEach((it) => scrollObserver.unobserve(it.target));
    const elements = document.querySelectorAll('.site-link');
    let anySiteLinkFound = false;
    elements.forEach((element) => {
        if (element.textContent) {
            scrollObserver.observe(element);
            menuItems.value.push({
                id: element.id,
                name: element.textContent,
                hash: `#${element.id}`,
            });
            anySiteLinkFound = true;
        }
    });
    emit('update:hasItems', anySiteLinkFound);
}

function updateSelectedFromScroll(entries: IntersectionObserverEntry[]): void {
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

function updateSelectedFromRouteChange(): void {
    blockScrollObserver.value = true;
    currentHash.value = route.hash;
    setTimeout(() => (blockScrollObserver.value = false), 1500);
}

function scrollToId(id: string): void {
    const element = document.getElementById(id);
    if (element) {
        element.scrollIntoView({ behavior: 'smooth' });
    } else {
        console.error(`Element with id ${id} does not exist`);
    }
}

function init(): void {
    nextTick(updateSiteLinks);
    watch(
        () => route.path,
        () => nextTick(updateSiteLinks)
    );
    watch(
        () => route.hash,
        () => updateSelectedFromRouteChange()
    );
}

init();
</script>
