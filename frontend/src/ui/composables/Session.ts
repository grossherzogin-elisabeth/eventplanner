import type { Ref } from 'vue';
import { onBeforeUnmount, ref } from 'vue';
import { useAuthService } from '@/application';
import type { Permission, SignedInUser } from '@/domain';

export function useSession(): {
    signedInUser: Ref<SignedInUser | undefined>;
    hasPermission(permission: Permission): boolean;
} {
    console.log('init useSession');
    const authService = useAuthService();

    const signedInUser = ref<SignedInUser | undefined>(authService.getSignedInUser());
    const unregister = authService.onChange(() => (signedInUser.value = authService.getSignedInUser()));

    onBeforeUnmount(unregister);

    function hasPermission(permission: Permission): boolean {
        return signedInUser.value !== undefined && signedInUser.value.permissions.includes(permission);
    }

    return { signedInUser, hasPermission };
}
