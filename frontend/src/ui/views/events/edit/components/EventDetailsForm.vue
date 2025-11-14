<template>
    <section>
        <div class="mb-4">
            <VInputSelect
                :model-value="props.event.state"
                :label="$t('domain.event.status')"
                :options="eventStates.options.value"
                :errors="validation.errors.value['state']"
                :errors-visible="validation.showErrors.value"
                required
                :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                @update:model-value="update({ state: $event })"
            />
        </div>
        <div class="mb-4">
            <VInputText
                :model-value="props.event.name"
                :label="$t('domain.event.name')"
                :errors="validation.errors.value['name']"
                :errors-visible="validation.showErrors.value"
                required
                :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                @update:model-value="update({ name: $event })"
            />
        </div>
        <div class="mb-4">
            <VInputSelect
                :model-value="props.event.type"
                :label="$t('domain.event.category')"
                :options="eventTypes.options.value"
                :errors="validation.errors.value['type']"
                :errors-visible="validation.showErrors.value"
                required
                :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                @update:model-value="update({ type: $event })"
            />
        </div>
        <div class="mb-4">
            <VInputSelect
                :model-value="props.event.signupType"
                :label="$t('domain.event.signup-type')"
                :options="eventSignupTypes.options.value"
                :errors="validation.errors.value['signupType']"
                :errors-visible="validation.showErrors.value"
                required
                :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                @update:model-value="update({ signupType: $event })"
            />
        </div>
        <div class="mb-4">
            <VInputTextArea
                :model-value="props.event.description"
                :label="$t('domain.event.description')"
                hint="Markdown wird unterstützt"
                :errors="validation.errors.value['description']"
                :errors-visible="validation.showErrors.value"
                :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                @update:model-value="update({ description: $event })"
            />
        </div>
        <div class="mb-4 flex space-x-4">
            <div class="w-3/5">
                <VInputDate
                    :label="$t('domain.event.start-date')"
                    :model-value="props.event.start"
                    :highlight-from="props.event.start"
                    :highlight-to="props.event.end"
                    :errors="validation.errors.value['start']"
                    :errors-visible="validation.showErrors.value"
                    required
                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                    @update:model-value="update({ start: updateDate(event.start, $event) })"
                />
            </div>
            <div class="w-2/5">
                <VInputTime
                    :label="$t('domain.event.crew-on-board')"
                    :model-value="props.event.start"
                    :errors="validation.errors.value['start']"
                    :errors-visible="validation.showErrors.value"
                    required
                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                    @update:model-value="update({ start: updateTime(props.event.start, $event, 'minutes') })"
                />
            </div>
        </div>

        <div class="mb-4 flex space-x-4">
            <div class="w-3/5">
                <VInputDate
                    :label="$t('domain.event.end-date')"
                    :model-value="props.event.end"
                    :highlight-from="props.event.start"
                    :highlight-to="props.event.end"
                    :errors="validation.errors.value['end']"
                    :errors-visible="validation.showErrors.value"
                    required
                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                    @update:model-value="update({ end: updateDate(props.event.end, $event) })"
                />
            </div>
            <div class="w-2/5">
                <VInputTime
                    :label="$t('domain.event.crew-off-board')"
                    :model-value="props.event.end"
                    :errors="validation.errors.value['end']"
                    :errors-visible="validation.showErrors.value"
                    required
                    :disabled="!signedInUser.permissions.includes(Permission.WRITE_EVENT_DETAILS)"
                    @update:model-value="update({ end: updateTime(props.event.end, $event, 'minutes') })"
                />
            </div>
        </div>
    </section>
</template>
<script lang="ts" setup>
import { useAuthUseCase } from '@/application';
import { deepCopy, updateDate, updateTime } from '@/common';
import type { Event } from '@/domain';
import { Permission, useEventService } from '@/domain';
import { VInputDate, VInputSelect, VInputText, VInputTextArea, VInputTime } from '@/ui/components/common';
import { useEventSignupTypes } from '@/ui/composables/EventSignupTypes';
import { useEventStates } from '@/ui/composables/EventStates';
import { useEventTypes } from '@/ui/composables/EventTypes';
import { useValidation } from '@/ui/composables/Validation.ts';

interface Props {
    event: Event;
}

type Emits = (e: 'update:event', value: Event) => void;

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const eventStates = useEventStates();
const eventTypes = useEventTypes();
const eventSignupTypes = useEventSignupTypes();

const eventService = useEventService();
const authUseCase = useAuthUseCase();
const signedInUser = authUseCase.getSignedInUser();

const validation = useValidation(props.event, (evt) => (evt === null ? {} : eventService.validate(evt)));

function update(patch: Partial<Event>): void {
    const updatedEvent = Object.assign(deepCopy(props.event), patch);
    emit('update:event', updatedEvent);
}
</script>
