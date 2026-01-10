import { hasAnyOverlap } from '@/common';
import { Validator, matchesPattern, notContainedIn, notEmpty } from '@/common/validation';
import type { PositionKey, Qualification } from '@/domain';

export class QualificationService {
    private readonly keyPattern = /^[a-z-_0-9]*$/;
    private readonly uniqueKeyErrMessage = 'views.settings.qualifications.validation.key-must-be-unique';
    private readonly uniqueNameErrMessage = 'views.settings.qualifications.validation.name-must-be-unique';

    public validate(value: Qualification, allQualifications?: Qualification[]): Record<string, string[]> {
        const allKeys = allQualifications?.map((it) => it.key) ?? [];
        const allNames = allQualifications?.map((it) => it.name) ?? [];
        return Validator.validate('name', value.name, notEmpty(), notContainedIn(allNames, this.uniqueNameErrMessage))
            .validate('key', value.key, notEmpty(), notContainedIn(allKeys, this.uniqueKeyErrMessage), matchesPattern(this.keyPattern))
            .validate('description', value.description, notEmpty())
            .validate('icon', value.icon, notEmpty())
            .getErrors();
    }

    public doesQualificationMatchFilter(
        qualification: Qualification,
        filters: { text?: string; expires?: boolean; grantsPosition?: PositionKey[] }
    ): boolean {
        const filterTextLc = filters.text?.toLowerCase();
        if (
            filterTextLc &&
            !qualification.key.toLowerCase().includes(filterTextLc) &&
            !qualification.icon.toLowerCase().includes(filterTextLc) &&
            !qualification.name.toLowerCase().includes(filterTextLc) &&
            !qualification.description.toLowerCase().includes(filterTextLc)
        ) {
            return false;
        }
        if (
            filters.grantsPosition &&
            filters.grantsPosition.length > 0 &&
            !hasAnyOverlap(filters.grantsPosition, qualification.grantsPositions)
        ) {
            return false;
        }
        if (filters.expires && !qualification.expires) {
            return false;
        }
        return true;
    }
}
