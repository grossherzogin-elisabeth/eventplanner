import { useAuthService } from '@/application';
import type { Permission } from '@/domain';
import { mockSignedInUser } from '~/mocks';

export function setupUserPermissions(permissions: Permission[]): void {
    const authService = useAuthService();
    const signedInUser = mockSignedInUser();
    signedInUser.permissions.push(...permissions);
    authService.setSignedInUser(signedInUser);
}
