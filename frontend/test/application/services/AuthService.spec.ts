import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { AuthService } from '@/application';
import { resetApplicationServices } from '@/application';
import { useAuthService } from '@/application';
import { mockSignedInUser, mockUserDetails, mockUserMate } from '~/mocks';

describe('AuthService', () => {
    let testee: AuthService;

    beforeEach(() => {
        testee = useAuthService();
    });

    it('should return undefined if no user is signed in', () => {
        expect(testee.getSignedInUser()).toBeUndefined();
    });

    it('should return the signed in user', () => {
        const user = mockSignedInUser();
        testee.setSignedInUser(user);
        expect(testee.getSignedInUser()).toEqual(user);
    });

    it('should load signed in user from local storage', () => {
        const user = mockSignedInUser();
        localStorage.setItem('user', JSON.stringify(user));
        resetApplicationServices();
        const testee = useAuthService();
        expect(testee.getSignedInUser()).toEqual(user);
    });

    it('should clear the signedInUser if undefined is passed', () => {
        testee.setSignedInUser(mockSignedInUser());
        testee.setSignedInUser(undefined);
        expect(testee.getSignedInUser()).toBeUndefined();
    });

    it('should notify login listeners when a user signs in', () => {
        const loginCb = vi.fn();
        testee.onLogin(loginCb);
        const user = mockSignedInUser();
        testee.setSignedInUser(user);
        expect(loginCb).toHaveBeenCalledTimes(1);
        expect(loginCb).toHaveBeenCalledWith(user);
    });

    it('should notify logout listeners when a user signs out', () => {
        const logoutCb = vi.fn();
        testee.onLogout(logoutCb);
        testee.setSignedInUser(mockSignedInUser()); // Login first
        testee.setSignedInUser(undefined); // Then logout
        expect(logoutCb).toHaveBeenCalledTimes(1);
        expect(logoutCb).toHaveBeenCalledWith(undefined);
    });

    it('should notify change listeners on any user state change', () => {
        const changeCb = vi.fn();
        testee.onChange(changeCb);

        testee.setSignedInUser(mockSignedInUser()); // Login
        expect(changeCb).toHaveBeenCalledTimes(1);

        testee.setSignedInUser(undefined); // Logout
        expect(changeCb).toHaveBeenCalledTimes(2);
        expect(changeCb).toHaveBeenCalledWith(undefined);
    });

    it('onLogin should register a listener and return an unsubscribe function', () => {
        const loginCb = vi.fn();
        const unsubscribe = testee.onLogin(loginCb);
        const user = mockSignedInUser();
        testee.setSignedInUser(user);
        expect(loginCb).toHaveBeenCalledTimes(1);

        unsubscribe();
        testee.setSignedInUser(mockSignedInUser({ key: 'new-user' })); // Trigger again
        expect(loginCb).toHaveBeenCalledTimes(1); // Should not be called again after unsubscribe
    });

    it('onLogout should register a listener and return an unsubscribe function', () => {
        const logoutCb = vi.fn();
        const unsubscribe = testee.onLogout(logoutCb);
        testee.setSignedInUser(mockSignedInUser()); // Login
        testee.setSignedInUser(undefined); // Logout
        expect(logoutCb).toHaveBeenCalledTimes(1);

        unsubscribe();
        testee.setSignedInUser(mockSignedInUser()); // Login again
        testee.setSignedInUser(undefined); // Trigger logout again
        expect(logoutCb).toHaveBeenCalledTimes(1); // Should not be called again after unsubscribe
    });

    it('onChange should register a listener and return an unsubscribe function', () => {
        const changeCb = vi.fn();
        const unsubscribe = testee.onChange(changeCb);
        testee.setSignedInUser(mockSignedInUser()); // Trigger change
        expect(changeCb).toHaveBeenCalledTimes(1);

        unsubscribe();
        testee.setSignedInUser(undefined); // Trigger change again
        expect(changeCb).toHaveBeenCalledTimes(1); // Should not be called again after unsubscribe
    });

    it('should set the impersonating user and return the impersonated user', () => {
        const originalUser = mockSignedInUser();
        testee.setSignedInUser(originalUser);

        const impersonatedDetails = mockUserDetails(mockUserMate());
        const result = testee.impersonate(impersonatedDetails);

        expect(testee['impersonating']).toEqual(impersonatedDetails);
        expect(result.key).toBe(impersonatedDetails.key);
        expect(result.email).toBe(impersonatedDetails.email);
        expect(result.impersonated).toBe(true);
    });

    it('should return impersonated user details if impersonating', () => {
        const originalUser = mockSignedInUser();
        testee.setSignedInUser(originalUser);

        const impersonatedDetails = mockUserDetails(mockUserMate());
        testee.impersonate(impersonatedDetails);

        const result = testee.getSignedInUser();
        expect(result).toBeDefined();
        expect(result?.key).toBe(impersonatedDetails.key);
        expect(result?.email).toBe(impersonatedDetails.email);
        expect(result?.firstName).toBe(impersonatedDetails.firstName);
        expect(result?.lastName).toBe(impersonatedDetails.lastName);
        expect(result?.positions).toEqual(impersonatedDetails.positionKeys);
        expect(result?.impersonated).toBe(true);
        // auth related properties from the original user should remain
        expect(result?.roles).toEqual(originalUser.roles);
        expect(result?.permissions).toEqual(originalUser.permissions);
    });

    it('should clear impersonation if null is passed', () => {
        const originalUser = mockSignedInUser();
        testee.setSignedInUser(originalUser);
        testee.impersonate(mockUserDetails(mockUserMate())); // start impersonating
        expect(testee['impersonating']).not.toBeNull();

        const result = testee.impersonate(null); // stop impersonating
        expect(testee['impersonating']).toBeNull();
        expect(result).toEqual(originalUser); // should return the original user
    });

    it('should notify change listeners when impersonating', () => {
        const changeCb = vi.fn();
        testee.onChange(changeCb);

        testee.setSignedInUser(mockSignedInUser());
        changeCb.mockClear(); // Clear initial login call to isolate impersonate's effect

        testee.impersonate(mockUserDetails(mockUserMate()));
        expect(changeCb).toHaveBeenCalledTimes(1);
        expect(changeCb).toHaveBeenCalledWith(undefined);
    });
});
