import type {
    Event,
    Position,
    PositionKey,
    QualificationKey,
    Registration,
    RegistrationKey,
    Slot,
    User,
    UserKey,
} from '@/domain';
import type { ResolvedRegistrationSlot } from '@/domain/aggregates/ResolvedRegistrationSlot';

export class RegistrationService {
    public resolveRegistrations(event: Event, users: User[], positions: Position[]): ResolvedRegistrationSlot[] {
        const positionMap = new Map<PositionKey, Position>();
        positions.forEach((position) => positionMap.set(position.key, position));
        const userMap = new Map<UserKey, User>();
        users.forEach((user: User) => userMap.set(user.key, user));
        const slotMap = new Map<RegistrationKey, Slot>();
        event.slots
            .filter((slot) => slot.assignedRegistrationKey !== undefined)
            .forEach((slot) => slotMap.set(slot.assignedRegistrationKey || '', slot));

        const resolved: ResolvedRegistrationSlot[] = [];
        // add registrations
        event.registrations.forEach((registration: Registration) => {
            const user = userMap.get(registration.userKey || 'none');
            resolved.push({
                position: positionMap.get(registration.positionKey) || {
                    key: registration.positionKey,
                    name: registration.positionKey,
                    prio: 0,
                    color: '',
                },
                name: user ? `${user.nickName || user.firstName} ${user.lastName}` : registration.name || '',
                registration: registration,
                user: user,
                slot: slotMap.get(registration.key),
                expiredQualifications: this.filterExpiredQualifications(user, event.end),
                hasFitnessForSeaService: this.hasFitnessForSeaService(user, event.end),
                hasOverwrittenPosition: this.hasOverwrittenPosition(registration, user),
            });
        });
        // add empty slots
        event.slots
            .filter((slot) => !slot.assignedRegistrationKey)
            .forEach((slot) =>
                resolved.push({
                    position: positionMap.get(slot.positionKeys[0]) || {
                        key: slot.positionKeys[0],
                        name: slot.positionKeys[0],
                        prio: 0,
                        color: '',
                    },
                    name: '',
                    registration: undefined,
                    user: undefined,
                    slot: slot,
                    expiredQualifications: [],
                    hasFitnessForSeaService: false,
                    hasOverwrittenPosition: false,
                })
            );
        return resolved.sort(
            (a, b) =>
                b.position.prio - a.position.prio ||
                (!a.name && b.name ? 1 : 0) ||
                (!b.name && a.name ? -1 : 0) ||
                a.name.localeCompare(b.name)
        );
    }

    private hasFitnessForSeaService(user?: User, atTime: Date = new Date()): boolean {
        return (
            user?.qualifications?.find(
                (q) =>
                    q.qualificationKey === 'fitness-for-seaservice' &&
                    q.expiresAt &&
                    q.expiresAt.getTime() > atTime.getTime()
            ) !== undefined
        );
    }

    private hasOverwrittenPosition(registration: Registration, user?: User): boolean {
        return user !== undefined && !user.positionKeys.includes(registration.positionKey);
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
