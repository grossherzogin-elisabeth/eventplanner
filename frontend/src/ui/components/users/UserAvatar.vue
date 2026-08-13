<template>
    <div class="flex items-center justify-center rounded-full bg-current/10">
        <img
            v-if="config.enableGravatar && props.user?.emailHash"
            :src="userService.resolveGravatarUrl(props.user)"
            alt="Avatar"
            class="h-full w-full rounded-full object-cover"
        />
        <i v-else class="fa-solid fa-user text-xs text-current" />
    </div>
</template>

<script lang="ts" setup>
import type { User } from '@/domain';
import { useUserService } from '@/domain';
import { useConfig } from '@/ui/composables/Config.ts';

interface Props {
    user?: User;
}

const props = defineProps<Props>();

const { config } = useConfig();
const userService = useUserService();
</script>
