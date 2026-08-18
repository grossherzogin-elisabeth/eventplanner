<template>
    <div class="full-width-scrollable">
        <VTable :items="props.registrations" class="scrollbar-invisible interactive-table no-header" :page-size="-1">
            <template #icon="{ item }">
                <template v-if="item?.state === RegistrationSlotState.OPEN">
                    <div class="bg-error-container/20 flex h-full w-full items-center justify-center rounded-full">
                        <i class="fa-solid fa-question text-error text-xs sm:text-sm"></i>
                    </div>
                </template>
                <template v-else>
                    <UserAvatar
                        :user="item?.user"
                        class="h-full w-full"
                        :class="item?.state === RegistrationSlotState.CONFIRMED ? 'text-success' : 'text-secondary'"
                    />
                    <span v-if="getIconForState(item?.state) != undefined">
                        <i
                            class="fa-solid fa-check-circle absolute -right-1 -bottom-1 text-xs sm:text-sm"
                            :class="getIconForState(item?.state)"
                        />
                    </span>
                </template>
            </template>
            <template #row="{ item }">
                <template v-if="item?.registration">
                    <td class="w-full">
                        <VTooltip :delay="500">
                            <template #default>
                                <p class="mb-1 flex items-center gap-x-1 font-semibold">
                                    {{ item.name || $t('domain.registration.unknown-user') }}
                                </p>
                                <p v-if="item.registration?.note" class="text-onsurface-variant/75 mb-2 line-clamp-2 text-sm italic">
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
                                        {{ $t('domain.user-qualification.expired-count', item.expiredQualifications.length) }}
                                    </span>
                                    <span v-else class="tag success">
                                        <i class="fa-solid fa-check" />
                                        {{ $t('domain.user-qualification.valid', 2) }}
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
                        <template v-if="item">
                            {{ $t('domain.event-slot.empty') }}
                        </template>
                    </p>
                    <p v-if="item?.slot" class="flex items-center gap-x-1 gap-y-2 opacity-50">
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
                <RouterLink
                    :to="{ name: Routes.UserDetails, params: { key: item.user?.key } }"
                    target="_blank"
                    class="context-menu-item"
                    :class="{ disabled: !item.user }"
                >
                    <i class="fa-solid fa-arrow-up-right-from-square"></i>
                    <span>{{ $t('domain.user.actions.view') }}</span>
                </RouterLink>
                <template v-if="event?.signupType === EventSignupType.Assignment">
                    <li
                        v-if="item.slot"
                        class="context-menu-item"
                        :class="{ disabled: !item.registration }"
                        @click="emit('removeFromCrew', item)"
                    >
                        <i class="fa-solid fa-hourglass-half"></i>
                        <span>{{ $t('domain.registration.actions.unassign') }}</span>
                    </li>
                    <li v-else class="context-menu-item" :class="{ disabled: !item.registration }" @click="emit('addToCrew', item)">
                        <i class="fa-solid fa-user-plus"></i>
                        <span>{{ $t('domain.registration.actions.assign') }}</span>
                    </li>
                </template>
                <li class="context-menu-item" :class="{ disabled: !item.registration }" @click="emit('editRegistration', item)">
                    <i class="fa-solid fa-clipboard-list"></i>
                    <span>{{ $t('domain.registration.actions.edit') }}</span>
                </li>
                <li v-if="item.slot" class="context-menu-item" @click="emit('editSlot', item)">
                    <i class="fa-solid fa-edit"></i>
                    <span>{{ $t('domain.event-slot.actions.edit') }}</span>
                </li>
                <li v-if="item.registration" class="context-menu-item text-error" @click="emit('deleteRegistration', item)">
                    <i class="fa-solid fa-trash-alt"></i>
                    <span>{{ $t('domain.registration.actions.delete') }}</span>
                </li>
                <li v-else class="context-menu-item text-error" @click="emit('deleteSlot', item)">
                    <i class="fa-solid fa-trash-alt"></i>
                    <span>{{ $t('domain.event-slot.actions.delete') }}</span>
                </li>
            </template>
        </VTable>
    </div>
</template>
<script lang="ts" setup>
import type { Event, ResolvedRegistrationSlot } from '@/domain';
import { EventSignupType, RegistrationSlotState } from '@/domain';
import { VTable, VTooltip } from '@/ui/components/common';
import { usePositions } from '@/ui/composables/Positions.ts';
import { Routes } from '@/ui/views/Routes.ts';
import RegistrationTooltip from '@/ui/views/events/edit/components/RegistrationTooltip.vue';
import UserAvatar from '@/ui/components/users/UserAvatar.vue';

interface Props {
    event?: Event;
    registrations?: ResolvedRegistrationSlot[];
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

function getIconForState(state?: RegistrationSlotState): string | undefined {
    switch (state) {
        case RegistrationSlotState.WAITING_LIST:
            return 'fa-clock-four text-secondary';
        case RegistrationSlotState.CONFIRMED:
            return 'fa-check-circle text-success';
    }
    return undefined;
}
</script>
