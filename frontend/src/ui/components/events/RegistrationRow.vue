<template>
    <li class="flex items-center space-x-2 md:space-x-4">
        <UserAvatar :user="registration.user" class="h-7 w-7" :class="props.registration.name ? 'text-secondary' : 'text-error'" />
        <div class="w-0 grow truncate">
            <RouterLink
                v-if="props.registration.user && hasPermission(Permission.READ_USER_DETAILS)"
                :to="{ name: Routes.UserDetails, params: { key: props.registration.user.key } }"
                class="hover:text-primary hover:underline"
            >
                {{ props.registration.name }}
            </RouterLink>
            <span v-else-if="props.registration.name">{{ props.registration.name }}</span>
            <span v-else-if="props.registration.user?.key" class="text-error italic">
                {{ $t('domain.registration.unknown-user') }}
            </span>
            <span v-else class="text-error/60 italic">
                {{ $t('domain.event-slot.empty') }}
            </span>
        </div>
        <span
            :style="{ '--color': props.registration.position.color }"
            class="tag custom max-w-[35vw] truncate"
            :class="{ 'opacity-50': !props.registration.registration }"
        >
            {{ props.registration.position.name }}
        </span>
    </li>
</template>

<script setup lang="ts">
import type { ResolvedRegistrationSlot } from '@/domain';
import { Permission } from '@/domain';
import { useSession } from '@/ui/composables/Session';
import { Routes } from '@/ui/views/Routes';
import UserAvatar from '@/ui/components/users/UserAvatar.vue';

interface Props {
    registration: ResolvedRegistrationSlot;
}

const props = defineProps<Props>();

const { hasPermission } = useSession();
</script>
