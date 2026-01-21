<template>
    <VTable
        :items="props.registrations"
        class="scrollbar-invisible interactive-table no-header overflow-x-auto px-8 md:px-16 xl:px-20"
        :page-size="-1"
    >
        <template #row="{ item }">
            <td class="w-0">
                <span v-if="item.state === RegistrationSlotState.WAITING_LIST">
                    <i class="fa-solid fa-hourglass-half opacity-25" />
                </span>
                <span v-else-if="item.state === RegistrationSlotState.CONFIRMED">
                    <i class="fa-solid fa-user-check text-success opacity-60" />
                </span>
                <span v-else-if="item.state === RegistrationSlotState.ASSIGNED">
                    <i class="fa-solid fa-user-clock opacity-25" />
                </span>
                <span v-else-if="item.state === RegistrationSlotState.OPEN">
                    <i class="fa-solid fa-user-xmark text-error/50" />
                </span>
            </td>
            <template v-if="item.registration">
                <td class="w-full">
                    <VTooltip :delay="500">
                        <template #default>
                            <p class="mb-1 flex items-center gap-x-1 font-semibold">
                                {{ item.name || $t('domain.registration.unknown-user') }}
                            </p>
                            <p v-if="item.registration?.note" class="text-onsurface-variant mb-2 line-clamp-2 text-sm italic">
                                <i class="fa-solid fa-comment-dots" />
                                {{ item.registration?.note.trim() }}
                            </p>
                            <p class="-mr-4 mb-1 flex flex-wrap items-center gap-x-1 gap-y-2">
                                <span
                                    :style="{ '--color': item.position.color }"
                                    class="tag"
                                    :class="item.hasOverwrittenPosition ? 'error line-through' : 'custom'"
                                >
                                    {{ item.slot?.positionName || item.position.name }}
                                </span>

                                <template v-if="!item.user || item.user?.qualifications?.length === 0"></template>
                                <span
                                    v-else-if="item.expiredQualifications.length > 0"
                                    class="tag error"
                                    :title="item.expiredQualifications.join(', ')"
                                >
                                    <i class="fa-solid fa-ban" />
                                    {{ $t('domain.user.qualification.expired-count', { count: item.expiredQualifications.length }) }}
                                </span>
                                <span v-else class="tag success">
                                    <i class="fa-solid fa-check" />
                                    {{ $t('domain.user.qualification.all-valid') }}
                                </span>

                                <span v-if="!item.registration?.userKey" class="tag info">
                                    <i class="fa-solid fa-info-circle" />
                                    {{ $t('domain.registration.guest') }}
                                </span>
                                <span v-if="item.registration?.overnightStay" class="tag info">
                                    <i class="fa-solid fa-bed" />
                                    {{ $t('domain.registration.overnight-stay') }}
                                </span>
                                <span v-if="item.registration?.arrival" class="tag info">
                                    <i class="fa-solid fa-calendar-day" />
                                    {{ $t('domain.registration.arrival-on-day-before') }}
                                </span>
                            </p>
                        </template>
                        <template #tooltip>
                            <RegistrationTooltip :registration="item" />
                        </template>
                    </VTooltip>
                </td>
            </template>
            <td v-else class="w-full">
                <p class="text-error mb-1 font-semibold italic opacity-50">
                    {{ $t('domain.event.slot.empty') }}
                </p>
                <p v-if="item.slot" class="flex items-center gap-x-1 gap-y-2 opacity-50">
                    <span
                        v-for="position in item.slot.positionKeys.map((it) => positions.get(it))"
                        :key="position.key"
                        :style="{ '--color': position.color }"
                        class="tag custom"
                    >
                        {{ position.name }}
                    </span>
                </p>
            </td>
        </template>
        <template #context-menu="{ item }">
            <ul>
                <li>
                    <RouterLink
                        :to="{ name: Routes.UserDetails, params: { key: item.user?.key } }"
                        target="_blank"
                        class="context-menu-item"
                        :class="{ disabled: !item.user }"
                    >
                        <i class="fa-solid fa-arrow-up-right-from-square"></i>
                        <span>{{ $t('views.events.edit.actions.show-user') }}</span>
                    </RouterLink>
                </li>
                <template v-if="event.signupType === EventSignupType.Assignment">
                    <li
                        v-if="item.slot"
                        class="context-menu-item"
                        :class="{ disabled: !item.registration }"
                        @click="emit('removeFromCrew', item)"
                    >
                        <i class="fa-solid fa-hourglass-half"></i>
                        <span>{{ $t('views.events.edit.actions.move-to-waiting-list') }}</span>
                    </li>
                    <li v-else class="context-menu-item" :class="{ disabled: !item.registration }" @click="emit('addToCrew', item)">
                        <i class="fa-solid fa-user-plus"></i>
                        <span>{{ $t('views.events.edit.actions.add-to-crew') }}</span>
                    </li>
                </template>
                <li class="context-menu-item" :class="{ disabled: !item.registration }" @click="emit('editRegistration', item)">
                    <i class="fa-solid fa-clipboard-list"></i>
                    <span>{{ $t('views.events.edit.actions.edit-registration') }}</span>
                </li>
                <li v-if="item.slot" class="context-menu-item" @click="emit('editSlot', item)">
                    <i class="fa-solid fa-edit"></i>
                    <span>{{ $t('views.events.edit.actions.edit-slot') }}</span>
                </li>
                <li v-if="item.registration" class="context-menu-item text-error" @click="emit('deleteRegistration', item)">
                    <i class="fa-solid fa-trash-alt"></i>
                    <span>{{ $t('views.events.edit.actions.delete-registration') }}</span>
                </li>
                <li v-else class="context-menu-item text-error" @click="emit('deleteSlot', item)">
                    <i class="fa-solid fa-trash-alt"></i>
                    <span>{{ $t('views.events.edit.actions.delete-slot') }}</span>
                </li>
            </ul>
        </template>
    </VTable>
</template>
<script lang="ts" setup>
import type { Event, ResolvedRegistrationSlot } from '@/domain';
import { EventSignupType, RegistrationSlotState } from '@/domain';
import { VTable, VTooltip } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions.ts';
import { Routes } from '@/ui/views/Routes.ts';
import RegistrationTooltip from '@/ui/views/events/edit/components/RegistrationTooltip.vue';

interface Props {
    event: Event;
    registrations: ResolvedRegistrationSlot[];
}

interface Emits {
    (e: 'addToCrew', value: ResolvedRegistrationSlot): void;
    (e: 'removeFromCrew', value: ResolvedRegistrationSlot): void;
    (e: 'editRegistration', value: ResolvedRegistrationSlot): void;
    (e: 'editSlot', value: ResolvedRegistrationSlot): void;
    (e: 'deleteRegistration', value: ResolvedRegistrationSlot): void;
    (e: 'deleteSlot', value: ResolvedRegistrationSlot): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const positions = usePositions();
</script>
