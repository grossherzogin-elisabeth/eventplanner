import { Validator, notEmpty } from '@/common/validation';
import type { Position } from '@/domain';

export class PositionService {
    public validate(position: Position): Record<string, string[]> {
        return Validator.validate('name', position.name, notEmpty())
            .validate('imoListRank', position.imoListRank, notEmpty())
            .validate('color', position.color, notEmpty())
            .getErrors();
    }
}
