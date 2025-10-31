<template>
    <section>
        <h2 class="mb-2 font-bold text-secondary">
            {{ $t('components.event-locations-card.title', { count: props.event.locations.length }) }}
        </h2>
        <div class="space-y-1 rounded-2xl bg-surface-container/50 p-4 pb-0 shadow xs:-mx-4">
            <p v-if="props.event.locations.length === 0" class="pb-4 text-sm">
                {{ $t('components.event-locations-card.placeholder') }}
            </p>

            <div v-else class="relative -ml-4">
                <div v-for="(location, index) in props.event.locations" :key="index" class="relative flex items-center">
                    <div class="flex w-12 flex-col items-center self-stretch">
                        <div class="my-1 flex h-7 w-7 items-center justify-center border-current">
                            <i class="fa-solid text-sm" :class="location.icon"></i>
                        </div>
                        <div
                            class="my-1 h-full"
                            :class="{ 'border-r-2 border-dashed border-current': index < props.event.locations.length - 1 }"
                        ></div>
                    </div>
                    <div class="mb-4 w-0 flex-grow">
                        <h3 class="mb-1 flex items-center justify-between space-x-2">
                            <span>{{ location.name }}</span>
                            <ContextMenuButton v-if="location.information">
                                <template #icon>
                                    <i class="fa-solid fa-info-circle text-primary/75 hover:text-primary/100"></i>
                                </template>
                                <template #default>
                                    <div class="overflow-hidden" @click.stop @mouseup.stop>
                                        <p class="text-sm">
                                            <VMarkdown :value="location.information" />
                                        </p>
                                        <p v-if="location.informationLink">
                                            <a :href="location.informationLink" class="link">
                                                {{ $t('components.event-locations-card.learn-more') }}
                                            </a>
                                        </p>
                                    </div>
                                </template>
                            </ContextMenuButton>
                        </h3>
                        <p v-if="location.addressLink" class="line-clamp-3 text-sm">
                            <a :href="location.addressLink" target="_blank" class="link">
                                {{ location.address || $t('components.event-locations-card.address-link') }}
                                <i class="fa-solid fa-external-link-alt mb-0.5 text-xs"></i>
                            </a>
                        </p>
                        <p v-else-if="location.address" class="line-clamp-3 text-sm">
                            {{ location.address }}
                        </p>
                        <p v-if="location.eta" class="text-sm">
                            <span class="inline-block"> {{ $t('components.event-locations-card.eta') }} </span>
                            {{ $d(location.eta, DateTimeFormat.DDD_DD_MM_hh_mm) }}
                        </p>
                        <p v-if="location.etd" class="text-sm">
                            <span class="inline-block"> {{ $t('components.event-locations-card.etd') }} </span>
                            {{ $d(location.etd, DateTimeFormat.DDD_DD_MM_hh_mm) }}
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
