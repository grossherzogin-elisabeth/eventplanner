import { Validator, notEmpty } from '@/common/validation';
import type { Qualification } from '@/domain';

export class QualificationService {
    public validate(qualification: Qualification): Record<string, string[]> {
        return Validator.validate('name', qualification.name, notEmpty())
            .validate('description', qualification.description, notEmpty())
            .validate('icon', qualification.icon, notEmpty())
            .getErrors();
    }
}
