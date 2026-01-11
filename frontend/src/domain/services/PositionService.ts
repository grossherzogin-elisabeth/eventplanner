import { Validator, matchesPattern, notContainedIn, notEmpty } from '@/common/validation';
import type { Position } from '@/domain';

export class PositionService {
    private readonly keyPattern = /^[a-z-_0-9]*$/;
    private readonly uniqueKeyErrMessage = 'views.settings.positions.validation.key-must-be-unique';
    private readonly uniqueNameErrMessage = 'views.settings.positions.validation.name-must-be-unique';

    public validate(value: Position, allPositions?: Position[]): Record<string, string[]> {
        const allKeys = allPositions?.map((it) => it.key) ?? [];
        const allNames = allPositions?.map((it) => it.name) ?? [];
        return Validator.validate('name', value.name, notEmpty(), notContainedIn(allNames, this.uniqueNameErrMessage))
            .validate('key', value.key, notEmpty(), notContainedIn(allKeys, this.uniqueKeyErrMessage), matchesPattern(this.keyPattern))
            .validate('imoListRank', value.imoListRank, notEmpty())
            .validate('color', value.color, notEmpty())
            .getErrors();
    }
}
