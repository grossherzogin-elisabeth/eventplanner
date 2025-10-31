import { NAME_REGEX, PHONE_REGEX } from '@/application/utils/RegExpresions.ts';
import { addToDate, compareBoolean } from '@/common';
import { Validator, before, doesNotContain, matchesPattern, maxLength, notEmpty } from '@/common/validation';
import type { Qualification, QualificationKey, User, UserDetails } from '@/domain';
import type { ResolvedUserQualification } from '@/domain/aggregates/ResolvedUserQualification';

export class UserService {
    public doesUserMatchFilter(user: User, filter: string): boolean {
        if (user.key === filter) {
            return true;
        }
        const filterLc = filter.toLowerCase();
        const fullname = `${user.firstName} ${user.lastName}`.toLowerCase().trim();
        if (fullname.includes(filterLc)) {
            return true;
        }
        const nickname = `${user.nickName} ${user.lastName}`.toLowerCase().trim();
        if (nickname.includes(filterLc)) {
            return true;
        }
        if (user.email?.toLowerCase().includes(filterLc)) {
            return true;
        }
        return false;
    }

    public resolveQualifications(user: UserDetails, qualifications: Map<QualificationKey, Qualification>): ResolvedUserQualification[] {
        const now = new Date();
        const expiresSoonDate = addToDate(now, { months: 3 });

        return user.qualifications
            .map((it) => {
                const qualification = qualifications.get(it.qualificationKey);
                const isExpired = it.expires && (!it.expiresAt || it.expiresAt.getTime() < now.getTime());
                const willExpireSoon = it.expires && (it.expiresAt ? it.expiresAt.getTime() < expiresSoonDate.getTime() : false);

                if (qualification) {
                    return {
                        ...qualification,
                        expiresAt: it.expiresAt,
                        note: it.note,
                        isExpired,
                        willExpireSoon,
                    };
                }

                return {
                    key: it.qualificationKey,
                    name: it.qualificationKey,
                    icon: 'fa-circle-question',
                    description: 'Unbekannte Qualification',
                    expires: it.expiresAt !== undefined,
                    expiresAt: it.expiresAt,
                    isExpired,
                    willExpireSoon,
                    grantsPositions: [],
                };
            })
            .sort(
                (a, b) =>
                    compareBoolean(a.isExpired, b.isExpired) ||
                    compareBoolean(a.willExpireSoon, b.willExpireSoon) ||
                    a.name.localeCompare(b.name)
            );
    }

    public getExpiredQualifications(user?: User, at: Date = new Date()): QualificationKey[] {
        if (!user?.qualifications) {
            return [];
        }
        const referenceTime = at.getTime();
        return user.qualifications
            .filter((it) => it.expires)
            .filter((it) => !it.expiresAt || it.expiresAt.getTime() <= referenceTime)
            .map((it) => it.qualificationKey);
    }

    public getSoonExpiringQualifications(user?: User, at: Date = new Date()): QualificationKey[] {
        const referenceTime = addToDate(at, { months: 3 });
        return this.getExpiredQualifications(user, referenceTime);
    }

    public validate(user: UserDetails | null): Record<string, string[]> {
        let errors: Record<string, string[]> = {};
        errors = Object.assign(errors, UserService.validateName(user));
        errors = Object.assign(errors, UserService.validateDateAndPlaceOfBirth(user));
        errors = Object.assign(errors, UserService.validateDiseases(user));
        errors = Object.assign(errors, UserService.validateMedication(user));
        errors = Object.assign(errors, UserService.validateIntolerances(user));
        errors = Object.assign(errors, UserService.validateEmergencyContact(user));
        errors = Object.assign(errors, UserService.validatePassNr(user));
        errors = Object.assign(errors, UserService.validateAddress(user));
        errors = Object.assign(errors, UserService.validatePhone(user));
        errors = Object.assign(errors, UserService.validatePhoneWork(user));
        errors = Object.assign(errors, UserService.validateMobile(user));
        return errors;
    }

    public static validateName(user: UserDetails | null): Record<string, string[]> {
        return Validator.validate(
            'nickName',
            user?.nickName,
            maxLength(20),
            matchesPattern(NAME_REGEX, 'generic.validation.invalid-name'),
            doesNotContain(user?.lastName, 'generic.validation.must-not-include-lastname')
        ).getErrors();
    }

    public static validateDateAndPlaceOfBirth(user: UserDetails | null): Record<string, string[]> {
        return Validator.validate('dateOfBirth', user?.dateOfBirth, notEmpty(), before(new Date()))
            .validate('placeOfBirth', user?.placeOfBirth, notEmpty(), maxLength(100))
            .getErrors();
    }

    public static validateDiseases(user: UserDetails | null): Record<string, string[]> {
        return Validator.validate('diseases', user?.diseases, maxLength(1000)).getErrors();
    }

    public static validateMedication(user: UserDetails | null): Record<string, string[]> {
        return Validator.validate('medication', user?.medication, maxLength(1000)).getErrors();
    }

    public static validateIntolerances(user: UserDetails | null): Record<string, string[]> {
        return Validator.validate('intolerances', user?.intolerances, maxLength(1000)).getErrors();
    }

    public static validateEmergencyContact(user: UserDetails | null): Record<string, string[]> {
        return Validator.validate('emergencyContact.name', user?.emergencyContact.name, maxLength(200))
            .validate('emergencyContact.phone', user?.emergencyContact.phone, maxLength(200))
            .getErrors();
    }

    public static validatePassNr(user: UserDetails | null): Record<string, string[]> {
        return Validator.validate('passNr', user?.passNr, maxLength(50)).getErrors();
    }

    public static validateAddress(user: UserDetails | null): Record<string, string[]> {
        return Validator.validate('address.addressLine1', user?.address.addressLine1, notEmpty(), maxLength(200))
            .validate('address.town', user?.address.town, notEmpty(), maxLength(100))
            .validate('address.zipcode', user?.address.zipcode, notEmpty(), maxLength(10))
            .validate('address.country', user?.address.country, notEmpty(), maxLength(2))
            .getErrors();
    }

    public static validatePhone(user: UserDetails | null): Record<string, string[]> {
        return Validator.validate('phone', user?.phone, matchesPattern(PHONE_REGEX, 'generic.validation.invalid-phone-number')).getErrors();
    }

    public static validateMobile(user: UserDetails | null): Record<string, string[]> {
        return Validator.validate(
            'mobile',
            user?.mobile,
            matchesPattern(PHONE_REGEX, 'generic.validation.invalid-phone-number')
        ).getErrors();
    }

    public static validatePhoneWork(user: UserDetails | null): Record<string, string[]> {
        return Validator.validate(
            'phoneWork',
            user?.phoneWork,
            matchesPattern(PHONE_REGEX, 'generic.validation.invalid-phone-number')
        ).getErrors();
    }
}
