import { ArrayUtils } from '@/common';
import type {
    Event,
    Position,
    QualificationKey,
    Registration,
    ResolvedRegistration,
    ResolvedSlot,
    User,
} from '@/domain';

export class RegistrationService {
    public resolveRegistrationsWithAssignedSlots(
        event: Event,
        users: User[],
        positions: Map<string, Position>
    ): ResolvedSlot[] {
        return event.slots
            .map((slot) => {
                const registration = event.registrations.find((it) => it.key === slot.assignedRegistrationKey);
                if (registration) {
                    const user = this.resolveRegistrationUser(registration, users);
                    const position = positions.get(registration.positionKey) || this.unknownPosition();
                    return {
                        ...slot,
                        userName: user
                            ? `${user.nickName || user.firstName} ${user.lastName}`
                            : registration.name || '',
                        userKey: registration.userKey,
                        registration: registration,
                        position: position,
                        positionName: slot.positionName || position.name,
                        confirmed: Math.random() > 0.5,
                        expiredQualifications: this.filterExpiredQualifications(user, event.end),
                        hasOverwrittenPosition:
                            user !== undefined && !user.positionKeys.includes(registration.positionKey),
                        hasFitnessForSeaService:
                            user?.qualifications?.find(
                                (q) =>
                                    q.qualificationKey === 'fitness-for-seaservice' &&
                                    q.expiresAt &&
                                    q.expiresAt.getTime() > event.end.getTime()
                            ) !== undefined,
                    };
                }
                const position = positions.get(slot.positionKeys[0]) || this.unknownPosition();
                return {
                    ...slot,
                    userName: undefined,
                    userKey: undefined,
                    registration: registration,
                    position: position,
                    positionName: slot.positionName || position.name,
                    confirmed: false,
                    expiredQualifications: [],
                    hasOverwrittenPosition: false,
                    hasFitnessForSeaService: false,
                };
            })
            .sort(
                (a, b) =>
                    b.position.prio - a.position.prio ||
                    a.order - b.order ||
                    a.userName?.localeCompare(b.userName || '') ||
                    0
            );
    }

    public resolveRegistrationsOnWaitingList(
        event: Event,
        users: User[],
        positions: Map<string, Position>
    ): ResolvedRegistration[] {
        const assignedRegistrationKeys = event.slots
            .map((it) => it.assignedRegistrationKey)
            .filter(ArrayUtils.filterUndefined);
        return event.registrations
            .filter((registration) => !assignedRegistrationKeys.includes(registration.key))
            .map((registration) => {
                const user = this.resolveRegistrationUser(registration, users);
                return {
                    ...registration,
                    name: this.resolveRegistrationUserName(registration, users),
                    position: positions.get(registration.positionKey) || this.unknownPosition(),
                    user: user,
                    expiredQualifications: this.filterExpiredQualifications(user, event.end),
                    hasOverwrittenPosition: user !== undefined && !user.positionKeys.includes(registration.positionKey),
                    hasFitnessForSeaService:
                        user?.qualifications?.find(
                            (q) =>
                                q.qualificationKey === 'fitness-for-seaservice' &&
                                q.expiresAt &&
                                q.expiresAt.getTime() > event.end.getTime()
                        ) !== undefined,
                };
            })
            .filter(ArrayUtils.filterUndefined)
            .sort(
                (a, b) =>
                    b.position.prio - a.position.prio ||
                    (b.hasFitnessForSeaService ? 1 : 0) - (a.hasFitnessForSeaService ? 1 : 0) ||
                    a.name.localeCompare(b.name)
            );
    }

    private resolveRegistrationUser(registration: Registration, users: User[]): User | undefined {
        if (!registration.userKey) {
            return undefined;
        }
        return users.find((user) => user.key === registration.userKey);
    }

    private resolveRegistrationUserName(registration: Registration, users: User[]): string {
        const user = this.resolveRegistrationUser(registration, users);
        if (!user) {
            return registration.name || '';
        }
        return `${user.nickName || user.firstName} ${user.lastName}`.trim();
    }

    private unknownPosition(): Position {
        return { key: '', name: '?', prio: 0, color: '' };
    }

    private filterExpiredQualifications(user?: User, date: Date = new Date()): QualificationKey[] {
        if (!user?.qualifications) {
            return [];
        }
        return user.qualifications
            .filter((it) => it.expiresAt !== undefined && it.expiresAt.getTime() <= date.getTime())
            .map((it) => it.qualificationKey);
    }
}
