<template>
    <div class="xs:-mx-8 -mx-4 md:-mx-16 xl:-mx-20">
        <VTable
            :items="props.events"
            :page-size="-1"
            class="scrollbar-invisible interactive-table no-header xs:px-8 overflow-x-auto px-4 md:px-16 xl:px-20"
            @click="openEvent($event.item, $event.event)"
        >
            <template #row="{ item }">
                <UserEventRow :event="item" :user="props.user" />
            </template>
            <template v-if="hasPermission(Permission.WRITE_USERS)" #context-menu="{ item }">
                <li>
                    <RouterLink
                        :to="{
                            name: Routes.EventDetails,
                            params: { year: item.start.getFullYear(), key: item.key },
                        }"
                        data-test-id="action-view-event"
                        class="context-menu-item"
                    >
                        <i class="fa-solid fa-search" />
                        <span>Veranstaltung anzeigen</span>
                    </RouterLink>
                </li>
                <li v-if="hasPermission(Permission.WRITE_EVENTS)">
                    <RouterLink
                        :to="{
                            name: Routes.EventEdit,
                            params: { year: item.start.getFullYear(), key: item.key },
                        }"
                        data-test-id="action-edit-event"
                        class="context-menu-item"
                    >
                        <i class="fa-solid fa-drafting-compass" />
                        <span>Veranstaltung bearbeiten</span>
                    </RouterLink>
                </li>
            </template>
        </VTable>
    </div>
</template>
<script setup lang="ts">
import type { RouteLocationRaw } from 'vue-router';
import { useRouter } from 'vue-router';
import type { Event, UserDetails } from '@/domain';
import { Permission } from '@/domain';
import { VTable } from '@/ui/components/common';
import { useSession } from '@/ui/composables/Session.ts';
import { Routes } from '@/ui/views/Routes.ts';
import UserEventRow from '@/ui/views/users/details/tabs/UserEventRow.vue';

interface Props {
    events?: Event[];
    user: UserDetails;
}

const props = defineProps<Props>();

const router = useRouter();
const { hasPermission } = useSession();

async function openEvent(item: Event, evt: MouseEvent): Promise<void> {
    let to: RouteLocationRaw = {
        name: Routes.EventDetails,
        params: { year: item.start.getFullYear(), key: item.key },
    };
    if (hasPermission(Permission.WRITE_EVENTS)) {
        to = {
            name: Routes.EventEdit,
            params: { year: item.start.getFullYear(), key: item.key },
        };
    }
    if (evt.metaKey || evt.ctrlKey) {
        window.open(router.resolve(to).href, '_blank');
    } else {
        await router.push(to);
    }
}
</script>
