<template>
    <VTable :items="userRoles" class="scrollbar-invisible no-header xs:px-8 overflow-x-auto px-4 md:px-16 xl:px-20">
        <template #row="{ item }">
            <td class="hidden w-0 pr-4 md:table-cell">
                <i class="fa-solid text-2xl" :class="item?.icon"></i>
            </td>
            <td class="w-full sm:w-2/3 sm:min-w-80">
                <div class="mb-2 flex items-center justify-between gap-2">
                    <p class="mb-1 w-0 grow truncate font-semibold">
                        <span class="xs:inline hidden md:hidden">
                            <i class="fa-solid pr-2" :class="item?.icon"></i>
                        </span>
                        {{ item?.name }}
                    </p>
                    <div v-if="item?.enabled" class="status-badge success text-xs sm:hidden" data-test-id="status-assigned">
                        <i class="fa-solid fa-check-circle"></i>
                        <span>zugewiesen</span>
                    </div>
                    <div v-else class="status-badge neutral text-xs sm:hidden" data-test-id="status-not-assigned">
                        <i class="fa-solid fa-xmark-circle"></i>
                        <span>nicht zugewiesen</span>
                    </div>
                </div>
                <p class="text-sm">{{ item?.description }}</p>
            </td>
            <td class="hidden w-1/3 min-w-48 sm:table-cell">
                <div class="flex items-center justify-end">
                    <button
                        v-if="item?.enabled"
                        class="status-badge success"
                        data-test-id="status-assigned"
                        @click="toggleRole(item?.role)"
                    >
                        <i class="fa-solid fa-check-circle"></i>
                        <span>zugewiesen</span>
                    </button>
                    <button v-else class="status-badge neutral" data-test-id="status-not-assigned" @click="toggleRole(item?.role)">
                        <i class="fa-solid fa-xmark-circle"></i>
                        <span>nicht zugewiesen</span>
                    </button>
                </div>
            </td>
        </template>
        <template v-if="hasPermission(Permission.WRITE_USERS)" #context-menu="{ item }">
            <li v-if="!item.enabled" class="context-menu-item" data-test-id="action-add-role" @click="toggleRole(item.role)">
                <i class="fa-solid fa-plus" />
                <span>Rolle hinzufügen</span>
            </li>
            <li v-else class="context-menu-item text-error" data-test-id="action-remove-role" @click="toggleRole(item.role)">
                <i class="fa-solid fa-xmark" />
                <span>Rolle entfernen</span>
            </li>
        </template>
    </VTable>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import type { UserDetails } from '@/domain';
import { Permission, Role } from '@/domain';
import { VTable } from '@/ui/components/common';
import { useSession } from '@/ui/composables/Session.ts';

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

const { t } = useI18n();
const { hasPermission } = useSession();

const roles = ref<RoleTableEntry[]>([
    {
        role: Role.TEAM_MEMBER,
        name: t('domain.role.ROLE_TEAM_MEMBER'),
        icon: 'fa-sailboat',
        description:
            'Der Nutzer kann sich eigenständig zu Veranstaltungen an- und abmelden und alle Veranstaltungen sowie verknüpfte andere Nutzer sehen',
        enabled: false,
    },
    {
        role: Role.EVENT_PLANNER,
        name: t('domain.role.ROLE_EVENT_PLANNER'),
        icon: 'fa-map',
        description: 'Der Nutzer kann Veranstaltungen bearbeiten und neue Veranstaltungen einstellen.',
        enabled: false,
    },
    {
        role: Role.TEAM_PLANNER,
        name: t('domain.role.ROLE_TEAM_PLANNER'),
        icon: 'fa-compass-drafting',
        description: 'Der Nutzer kann die Crew einer Veranstaltung bearbeiten',
        enabled: false,
    },
    {
        role: Role.USER_MANAGER,
        name: t('domain.role.ROLE_USER_MANAGER'),
        icon: 'fa-people-group',
        description: 'Der Nutzer kann Nutzer bearbeiten, Qualifikationen pflegen und neue Nutzer erstellen',
        enabled: false,
    },
    {
        role: Role.EVENT_LEADER,
        name: t('domain.role.ROLE_EVENT_LEADER'),
        icon: 'fa-life-ring',
        description: 'Der Nutzer kann die aktuelle Veranstaltung bearbeiten um Last-Minute Änderungen an der Crewliste vornehmen zu können',
        enabled: false,
    },
    {
        role: Role.ADMIN,
        name: t('domain.role.ROLE_ADMIN'),
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

function toggleRole(role?: Role): void {
    if (role) {
        const user = props.modelValue;
        if (user.roles.includes(role)) {
            user.roles = user.roles.filter((r) => r !== role);
        } else {
            user.roles.push(role);
        }
        emit('update:modelValue', user);
    }
}
</script>
