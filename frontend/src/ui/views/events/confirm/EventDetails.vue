<template>
    <table class="block sm:table">
        <tbody>
            <tr class="mb-2 block sm:table-row">
                <td class="block pr-8 text-xs sm:table-cell sm:py-1 sm:text-base">Reise:</td>
                <td class="block py-1 font-bold sm:table-cell">
                    {{ props.event?.name }}
                </td>
            </tr>
            <tr class="mb-2 block sm:table-row">
                <td class="block pr-8 text-xs sm:table-cell sm:py-1 sm:text-base">Geplante Route:</td>
                <td class="block py-1 font-bold sm:table-cell">
                    {{ props.event.locations.map((l) => l.name).join(' - ') }}
                </td>
            </tr>
            <tr class="mb-2 block sm:table-row">
                <td class="block pr-8 text-xs sm:table-cell sm:py-1 sm:text-base">Crew an Bord:</td>
                <td class="block py-1 font-bold sm:table-cell">
                    {{ $d(props.event.start, DateTimeFormat.DD_MM_YYYY) }}
                    {{ $d(props.event.start, DateTimeFormat.hh_mm) }}
                </td>
            </tr>
            <tr v-if="props.event.locations[0].etd" class="mb-2 block sm:table-row">
                <td class="block pr-8 text-xs sm:table-cell sm:py-1 sm:text-base">Reisebeginn:</td>
                <td class="block py-1 font-bold sm:table-cell">
                    {{ $d(props.event.locations[0].etd, DateTimeFormat.DD_MM_YYYY) }}
                    {{ $d(props.event.locations[0].etd, DateTimeFormat.hh_mm) }}
                </td>
            </tr>
        </tbody>
    </table>
</template>
<script setup lang="ts">
import { DateTimeFormat } from '@/common/date';
import type { Event } from '@/domain';

interface Props {
    event: Event;
}
const props = defineProps<Props>();
</script>
