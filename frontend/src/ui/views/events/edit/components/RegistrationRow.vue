<template>
    <VTooltip :disabled="props.value.registration === undefined" :delay="500">
        <template #default>
            <ContextMenuButton class="w-full" anchor-align-x="right" dropdown-position-x="left">
                <template #icon>
                    <VDraggable
                        class="flex items-center rounded-xl px-4 py-2 hover:bg-surface-container md:space-x-4"
                        :class="{
                            'cursor-move': props.value.registration !== undefined,
                            'pointer-events-none': props.value.registration === undefined,
                            '': props.value.expiredQualifications.length === 0,
                        }"
                        component="li"
                        :value="props.value"
                        @dragend="emit('dragend')"
                        @dragstart="emit('dragstart')"
                    >
                        <!-- drag icon -->
                        <i class="fa-solid fa-grip-vertical hidden text-sm text-onsurface-variant opacity-50 lg:inline"></i>

                        <template v-if="props.value.slot">
                            <!-- user status -->
                            <span v-if="props.value.name && !props.value.registration?.confirmed">
                                <i class="fa-solid fa-user-clock opacity-25"></i>
                            </span>
                            <span v-else-if="props.value.name && props.value.registration?.confirmed">
                                <i class="fa-solid fa-user-check text-green opacity-60"></i>
                            </span>
                            <span v-else>
                                <i class="fa-solid fa-user-xmark text-error text-opacity-50"></i>
                            </span>
                        </template>

                        <!-- user name -->
                        <span v-if="props.value.name" class="mx-2 truncate">
                            {{ props.value.name }}
                            <template v-if="!props.value.user"> (Gastcrew) </template>
                        </span>
                        <span v-else-if="props.value.user" class="mx-2 truncate italic text-error"> Gelöschter Nutzer </span>
                        <span v-else class="mx-2 truncate italic text-error text-opacity-50"> Noch nicht besetzt </span>

                        <i
                            v-if="props.value.registration?.note"
                            class="fa-solid fa-comment-dots text-sm text-onsurface-variant opacity-50"
                        />
                        <!-- user position -->
                        <span class="flex-grow"></span>
                        <span :style="{ background: props.value.position.color }" class="position text-xs">
                            {{ props.value.slot?.positionName || props.value.position.name }}
                            <i v-if="props.value.expiredQualifications.length > 0" class="fa-solid fa-warning text-error"></i>
                            <i v-else-if="props.value.registration && props.value.user" class="fa-solid fa-check"></i>
                            <i v-else-if="props.value.registration && !props.value.user" class="fa-solid fa-question"></i>
                        </span>
                    </VDraggable>
                </template>
                <template #default>
                    <ul>
                        <RouterLink
                            :to="{ name: Routes.UserDetails, params: { key: props.value.user?.key } }"
                            target="_blank"
                            class="context-menu-item"
                            :class="{ disabled: !props.value.user }"
                        >
                            <i class="fa-solid fa-arrow-up-right-from-square"></i>
                            <span>{{ $t('views.events.edit.actions.show-user') }}</span>
                        </RouterLink>
                        <li
                            v-if="props.value.slot"
                            class="context-menu-item"
                            :class="{ disabled: !props.value.registration }"
                            @click="emit('removeFromTeam')"
                        >
                            <i class="fa-solid fa-arrow-right"></i>
                            <span>{{ $t('views.events.edit.actions.move-to-waiting-list') }}</span>
                        </li>
                        <li v-else class="context-menu-item" :class="{ disabled: !props.value.registration }" @click="emit('addToTeam')">
                            <i class="fa-solid fa-arrow-left"></i>
                            <span>{{ $t('views.events.edit.actions.add-to-crew') }}</span>
                        </li>
                        <li class="context-menu-item" :class="{ disabled: !props.value.registration }" @click="emit('editRegistration')">
                            <i class="fa-solid fa-edit"></i>
                            <span>{{ $t('views.events.edit.actions.edit-registration') }}</span>
                        </li>
                        <li v-if="props.value.slot" class="context-menu-item" @click="emit('editSlot')">
                            <i class="fa-solid fa-edit"></i>
                            <span>{{ $t('views.events.edit.actions.edit-slot') }}</span>
                        </li>
                        <li v-if="props.value.registration" class="context-menu-item text-error" @click="emit('cancelRegistration')">
                            <i class="fa-solid fa-user-minus"></i>
                            <span>{{ $t('views.events.edit.actions.delete-registration') }}</span>
                        </li>
                        <li v-else class="context-menu-item text-error" @click="emit('deleteSlot')">
                            <i class="fa-solid fa-trash-alt"></i>
                            <span>{{ $t('views.events.edit.actions.delete-slot') }}</span>
                        </li>
                    </ul>
                </template>
            </ContextMenuButton>
        </template>
        <template #tooltip>
            <RegistrationTooltip :registration="value" />
        </template>
    </VTooltip>
</template>
<script lang="ts" setup>
import type { ResolvedRegistrationSlot } from '@/domain';
import { ContextMenuButton, VDraggable, VTooltip } from '@/ui/components/common';
import { Routes } from '@/ui/views/Routes.ts';
import RegistrationTooltip from '@/ui/views/events/edit/components/RegistrationTooltip.vue';

interface Props {
    value: ResolvedRegistrationSlot;
}

interface Emits {
    (e: 'dragstart'): void;
    (e: 'dragend'): void;
    (e: 'edit'): void;
    (e: 'addToTeam'): void;
    (e: 'removeFromTeam'): void;
    (e: 'editRegistration'): void;
    (e: 'editSlot'): void;
    (e: 'cancelRegistration'): void;
    (e: 'deleteSlot'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
</script>
