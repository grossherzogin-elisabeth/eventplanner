<template>
    <section>
        <h2 class="mb-2 font-bold text-secondary">
            <template v-if="props.event.locations.length === 1">Ort</template>
            <template v-else>Route</template>
        </h2>
        <div class="space-y-1 rounded-2xl bg-surface-container-low p-4">
            <p v-if="props.event.locations.length === 0" class="text-sm">
                Für diese Reise wurde noch keine Reiseroute bekannt gegeben. Sobald diese Informationen verfügbar sind, kannst du sie hier
                sehen.
            </p>

            <div v-else class="relative -ml-4">
                <div class="absolute bottom-4 left-0 top-4 flex w-12 justify-center">
                    <div class="border-r-2 border-dashed border-current"></div>
                </div>
                <div v-for="(location, index) in props.event.locations" :key="index" class="relative mb-4 flex items-center last:mb-0">
                    <div class="flex w-12 flex-col items-center self-stretch">
                        <div class="-mt-1 flex h-7 w-7 items-center justify-center rounded-full border-current bg-surface-container-low">
                            <i class="fa-solid text-sm" :class="location.icon"></i>
                        </div>
                        <div v-if="index === props.event.locations.length - 1" class="w-full flex-1 bg-surface-container-low"></div>
                    </div>
                    <div class="w-0 flex-grow">
                        <h3 class="mb-1 flex items-center justify-between space-x-2">
                            <span>{{ location.name }}</span>
                            <ContextMenuButton v-if="location.information">
                                <template #icon>
                                    <i class="fa-solid fa-info-circle text-primary text-opacity-75 hover:text-opacity-100"></i>
                                </template>
                                <template #default>
                                    <div class="overflow-hidden" @click.stop @mouseup.stop>
                                        <p class="text-sm">
                                            <VMarkdown :value="location.information" />
                                        </p>
                                        <p v-if="location.informationLink">
                                            <a :href="location.informationLink" class="link">Weitere Informationen</a>
                                        </p>
                                    </div>
                                </template>
                            </ContextMenuButton>
                        </h3>
                        <p v-if="location.addressLink" class="line-clamp-3 text-sm">
                            <a :href="location.addressLink" target="_blank" class="link">
                                {{ location.address || 'Anreiseinformationen' }}
                                <i class="fa-solid fa-external-link-alt mb-0.5 text-xs"></i>
                            </a>
                        </p>
                        <p v-else-if="location.address" class="line-clamp-3 text-sm">
                            {{ location.address }}
                        </p>
                        <p v-if="location.eta" class="text-sm">
                            <span class="inline-block">Ankunft:</span> {{ $d(location.eta, DateTimeFormat.DDD_DD_MM_hh_mm) }}
                        </p>
                        <p v-if="location.etd" class="text-sm">
                            <span class="inline-block">Abfahrt:</span> {{ $d(location.etd, DateTimeFormat.DDD_DD_MM_hh_mm) }}
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </section>
</template>
<script lang="ts" setup>
import { DateTimeFormat } from '@/common/date';
import type { Event } from '@/domain';
import { ContextMenuButton, VMarkdown } from '@/ui/components/common';

interface Props {
    event: Event;
}
const props = defineProps<Props>();
</script>
