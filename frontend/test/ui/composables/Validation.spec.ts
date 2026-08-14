import type { ComputedRef } from 'vue';
import { computed, nextTick, ref } from 'vue';
import { describe, expect, it, vi } from 'vitest';
import { Validator, after, maxLength, minLength, notEmpty } from '@/common/validation';
import { useValidation } from '@/ui/composables/Validation';
import { withSetup } from '~/utils';

describe('useValidation', () => {
    it('should initialize with empty errors and valid state', () => {
        const { instance: validation } = withSetup(() => {
            const value = ref({ name: 'test' });
            return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
        });

        expect(validation.errors.value).toEqual({});
        expect(validation.isValid.value).toBe(true);
        expect(validation.showErrors.value).toBe(false);
        expect(validation.disableSubmit.value).toBe(false);
    });

    it('should initialize with showErrors false even if there are validation errors', async () => {
        const { instance: validation } = withSetup(() => {
            const value = ref({ name: '' });
            return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
        });

        await nextTick();

        expect(validation.errors.value).toHaveProperty('name');
        expect(validation.showErrors.value).toBe(false);
        expect(validation.disableSubmit.value).toBe(false);
    });

    it('should validate on initialization', async () => {
        const validationFn = vi.fn((v) => Validator.validate('name', v.name, notEmpty()).getErrors());

        const { instance: validation } = withSetup(() => {
            const value = ref({ name: '' });
            return useValidation(value, validationFn);
        });

        await nextTick();

        expect(validationFn).toHaveBeenCalled();
        expect(validation.errors.value).toHaveProperty('name');
        expect(validation.isValid.value).toBe(false);
    });

    it('should validate when calling validate() explicitly', () => {
        const validationFn = vi.fn((v) => Validator.validate('name', v.name, notEmpty()).getErrors());

        const { instance: validation } = withSetup(() => {
            const value = ref({ name: 'test' });
            return useValidation(value, validationFn);
        });

        validation.validate();

        expect(validationFn).toHaveBeenCalled();
    });

    it('should re-validate when the value ref changes', async () => {
        const { instance } = withSetup(() => {
            const value = ref({ name: 'test' });
            return {
                value,
                validation: useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors()),
            };
        });

        expect(instance.validation.isValid.value).toBe(true);

        instance.value.value = { name: '' };
        await nextTick();

        expect(instance.validation.isValid.value).toBe(false);
        expect(instance.validation.errors.value).toHaveProperty('name');
    });

    it('should work with computed refs as input', async () => {
        const { instance } = withSetup(() => {
            const source = ref({ name: 'test' });
            const computed_value: ComputedRef<{ name: string }> = computed(() => source.value);

            return {
                source,
                validation: useValidation(computed_value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors()),
            };
        });

        expect(instance.validation.isValid.value).toBe(true);

        instance.source.value = { name: '' };
        await nextTick();

        expect(instance.validation.isValid.value).toBe(false);
    });

    it('should work with non-ref values', () => {
        const { instance: validation } = withSetup(() => {
            const value = { name: 'test' };
            return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
        });

        expect(validation.isValid.value).toBe(true);
    });

    it('should translate error keys', async () => {
        const { instance: validation } = withSetup(() => {
            const value = ref({ name: '' });
            return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
        });

        await nextTick();

        const translated = validation.errors.value.name[0];
        expect(typeof translated).toBe('string');
        expect(translated).toEqual('Eingabe ist erforderlich');
    });

    it('should handle multiple errors for the same field', async () => {
        const { instance: validation } = withSetup(() => {
            const value = ref({ name: 'ab' });
            return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty(), minLength(3), maxLength(1)).getErrors());
        });

        await nextTick();

        expect(validation.errors.value.name).toHaveLength(2);
    });

    it('should accumulate errors from multiple field validations', async () => {
        const { instance: validation } = withSetup(() => {
            const value = ref({ name: '', email: '', age: '' });
            return useValidation(value, (v) =>
                Validator.validate('name', v.name, notEmpty())
                    .validate('email', v.email, notEmpty())
                    .validate('age', v.age, notEmpty())
                    .getErrors()
            );
        });

        await nextTick();

        expect(Object.keys(validation.errors.value)).toHaveLength(3);
        expect(validation.errors.value.name).toBeDefined();
        expect(validation.errors.value.email).toBeDefined();
        expect(validation.errors.value.age).toBeDefined();
    });

    it('should return raw error when i18n key does not exist', async () => {
        const { instance: validation } = withSetup(() => {
            const value = ref({ name: '' });
            return useValidation(value, () => ({
                name: ['non.existent.key'],
            }));
        });

        await nextTick();

        const translated = validation.errors.value.name[0];
        expect(translated).toBe('non.existent.key');
    });

    it('should substitute i18n parameters in error messages', async () => {
        const { instance: validation } = withSetup(() => {
            const value = ref({ name: 'a'.repeat(50) });
            return useValidation(value, (v) => Validator.validate('name', v.name, maxLength(35)).getErrors());
        });

        await nextTick();

        const translated = validation.errors.value.name[0];
        expect(translated).toContain('35');
    });

    it('should format date parameters in DD.MM.YYYY format', async () => {
        const { instance: validation } = withSetup(() => {
            const value = ref({ endDate: new Date('2024-01-10') });
            const startDate = new Date('2024-01-15');
            return useValidation(value, (v) => Validator.validate('endDate', v.endDate, after(startDate)).getErrors());
        });

        await nextTick();

        const translated = validation.errors.value.endDate[0];
        expect(translated).toBeTruthy();
        expect(translated).toContain('15.01.2024');
    });

    describe('isValid', () => {
        it('should set isValid to false when there are errors', async () => {
            const { instance: validation } = withSetup(() => {
                const value = ref({ name: '' });
                return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
            });

            await nextTick();

            expect(validation.isValid.value).toBe(false);
        });

        it('should set isValid to true when there are no errors', async () => {
            const { instance: validation } = withSetup(() => {
                const value = ref({ name: 'test' });
                return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
            });

            await nextTick();

            expect(validation.isValid.value).toBe(true);
        });

        it('should update isValid when validation state changes', async () => {
            const { instance } = withSetup(() => {
                const value = ref({ name: 'test' });
                return {
                    value,
                    validation: useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors()),
                };
            });

            expect(instance.validation.isValid.value).toBe(true);

            instance.value.value = { name: '' };
            await nextTick();

            expect(instance.validation.isValid.value).toBe(false);

            instance.value.value = { name: 'valid' };
            await nextTick();

            expect(instance.validation.isValid.value).toBe(true);
        });
    });

    describe('showErrors', () => {
        it('should start with showErrors false', () => {
            const { instance: validation } = withSetup(() => {
                const value = ref({ name: '' });
                return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
            });

            expect(validation.showErrors.value).toBe(false);
        });

        it('should allow setting showErrors to true', () => {
            const { instance: validation } = withSetup(() => {
                const value = ref({ name: '' });
                return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
            });

            validation.showErrors.value = true;

            expect(validation.showErrors.value).toBe(true);
        });

        it('should allow setting showErrors to false', () => {
            const { instance: validation } = withSetup(() => {
                const value = ref({ name: '' });
                return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
            });

            validation.showErrors.value = true;
            validation.showErrors.value = false;

            expect(validation.showErrors.value).toBe(false);
        });
    });

    describe('disableSubmit', () => {
        it('should disable submit when showErrors is true and isValid is false', async () => {
            const { instance: validation } = withSetup(() => {
                const value = ref({ name: '' });
                return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
            });

            await nextTick();

            validation.showErrors.value = true;

            expect(validation.disableSubmit.value).toBe(true);
        });

        it('should not disable submit when showErrors is false', async () => {
            const { instance: validation } = withSetup(() => {
                const value = ref({ name: '' });
                return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
            });

            await nextTick();

            expect(validation.disableSubmit.value).toBe(false);
        });

        it('should not disable submit when isValid is true', async () => {
            const { instance: validation } = withSetup(() => {
                const value = ref({ name: 'test' });
                return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
            });

            await nextTick();

            validation.showErrors.value = true;

            expect(validation.disableSubmit.value).toBe(false);
        });

        it('should toggle disableSubmit reactively', async () => {
            const { instance: validation } = withSetup(() => {
                const value = ref({ name: '' });
                return useValidation(value, (v) => Validator.validate('name', v.name, notEmpty()).getErrors());
            });

            await nextTick();

            expect(validation.disableSubmit.value).toBe(false);

            validation.showErrors.value = true;
            expect(validation.disableSubmit.value).toBe(true);

            validation.showErrors.value = false;
            expect(validation.disableSubmit.value).toBe(false);
        });
    });

    describe('watchEffect behavior', () => {
        it('should automatically validate when input ref changes', async () => {
            const validationFn = vi.fn((v) => Validator.validate('name', v.name, notEmpty()).getErrors());

            const { instance } = withSetup(() => {
                const value = ref({ name: 'initial' });
                return { value, validation: useValidation(value, validationFn) };
            });

            await nextTick();

            const initialCallCount = validationFn.mock.calls.length;

            instance.value.value = { name: '' };
            await nextTick();

            expect(validationFn.mock.calls.length).toBeGreaterThan(initialCallCount);
            expect(instance.validation.isValid.value).toBe(false);
        });

        it('should not cause infinite loops when validation result changes', async () => {
            let callCount = 0;

            withSetup(() => {
                const value = ref({ name: 'test' });
                return useValidation(value, (): Record<string, string[]> => {
                    callCount++;
                    return callCount > 5 ? { name: ['error'] } : {};
                });
            });

            await nextTick();
            await nextTick();

            expect(callCount).toBeLessThan(100);
        });
    });

    it('should handle validation function that throws an error', () => {
        expect(() => {
            withSetup(() => {
                const value = ref({ name: '' });
                return useValidation(value, () => {
                    throw new Error('Validation error');
                });
            });
        }).not.toThrow();
    });
});
