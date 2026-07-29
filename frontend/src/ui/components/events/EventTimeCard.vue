<template>
    <VInteractiveListItem
        :model-value="props.event"
        icon="fa-calendar-day"
        dialog-type="modal"
        :label="$t('domain.event.time')"
        :content="formatTimeRange(props.event.start, props.event.end)"
        readonly
    >
        <template #edit="{ value }">
            <VDatepicker :highlight-from="value.start" :highlight-to="value.end" readonly />
        </template>
    </VInteractiveListItem>
</template>
<script setup lang="ts">
import type { Event } from '@/domain';
import { VInteractiveListItem } from '@/ui/components/common';
import { formatTimeRange } from '@/ui/composables/DateRangeFormatter.ts';
import VDatepicker from '@/ui/components/common/datepicker/VDatepicker.vue';

interface Props {
    event: Event;
}

const props = defineProps<Props>();
</script>
