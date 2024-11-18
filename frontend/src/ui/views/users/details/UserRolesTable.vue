<template>
    <VTable :items="userRoles" class="scrollbar-invisible no-header overflow-x-auto px-8 md:px-16 xl:px-20">
        <template #row="{ item }">
            <td class="w-0">
                <i class="fa-solid" :class="item.icon"></i>
            </td>
            <td class="w-2/3 min-w-80">
                <p class="mb-1 font-semibold">{{ item.name }}</p>
                <p class="text-sm">
                    {{ item.description }}
                </p>
            </td>
            <td class="w-1/3 min-w-48">
                <div class="flex items-center justify-end" @click="toggleRole(item.role)">
                    <div v-if="item.enabled" class="status-panel bg-green-container text-ongreen-container">
                        <i class="fa-solid fa-check-circle"></i>
                        <span class="whitespace-nowrap font-semibold">zugewiesen</span>
                    </div>
                    <div v-else class="status-panel bg-surface-container-highest text-onsurface">
                        <i class="fa-solid fa-xmark-circle"></i>
                        <span class="whitespace-nowrap font-semibold">nicht zugewiesen</span>
                    </div>
                </div>
            </td>
        </template>
        <template #context-menu="{ item }">
            <li v-if="!item.enabled" class="context-menu-item" @click="toggleRole(item.role)">
                <i class="fa-solid fa-plus" />
                <span>Rolle hinzufügen</span>
            </li>
            <li v-else class="context-menu-item text-error" @click="toggleRole(item.role)">
                <i class="fa-solid fa-xmark" />
                <span>Rolle entfernen</span>
            </li>
        </template>
    </VTable>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import type { UserDetails } from '@/domain';
import { Role } from '@/domain';
import { VTable } from '@/ui/components/common';

interface Props {
    modelValue: UserDetails;
}

type Emit = (e: 'update:modelValue', user: UserDetails) => void;

interface RoleTableEntry {
    role: Role;
    icon: string;
    name: string;
    description: string;
    enabled: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<Emit>();

const roles = ref<RoleTableEntry[]>([
    {
        role: Role.TEAM_MEMBER,
        name: 'Stammcrewmitglied',
        icon: 'fa-sailboat',
        description: 'Der Nutzer kann sich eigenständig zu Reisen an- und abmelden und alle Reisen sowie verknüpfte andere Nutzer sehen',
        enabled: false,
    },
    {
        role: Role.EVENT_PLANNER,
        name: 'Reiseplaner:in',
        icon: 'fa-map',
        description: 'Der Nutzer kann Reisen bearbeiten und neue Reisen erstellen.',
        enabled: false,
    },
    {
        role: Role.TEAM_PLANNER,
        name: 'Crewplaner:in',
        icon: 'fa-compass-drafting',
        description: 'Der Nutzer kann die Crew einer Reise bearbeiten',
        enabled: false,
    },
    {
        role: Role.USER_MANAGER,
        name: 'Nutzerverwalter:in',
        icon: 'fa-people-group',
        description: 'Der Nutzer kann Nutzer bearbeiten, Qualifikationen pflegen und neue Nutzer erstellen',
        enabled: false,
    },
    {
        role: Role.EVENT_LEADER,
        name: 'Reiseleiter:in',
        icon: 'fa-life-ring',
        description: 'Der Nutzer kann die aktuelle Reise bearbeiten um Last-Minute Änderungen an der Crewliste vornehmen zu können',
        enabled: false,
    },
    {
        role: Role.ADMIN,
        name: 'Admin',
        icon: 'fa-wand-magic-sparkles',
        description: 'Der Nutzer kann alle Funktionen der App vollumfänglich nutzen und darf alle Daten sehen und bearbeiten',
        enabled: false,
    },
]);

const userRoles = computed<RoleTableEntry[]>(() => {
    return roles.value.map((p) => ({
        ...p,
        enabled: props.modelValue.roles.includes(p.role),
    }));
});

function toggleRole(role: Role): void {
    const user = props.modelValue;
    if (user.roles.includes(role)) {
        user.roles = user.roles.filter((r) => r !== role);
    } else {
        user.roles.push(role);
    }
    emit('update:modelValue', user);
}
</script>
