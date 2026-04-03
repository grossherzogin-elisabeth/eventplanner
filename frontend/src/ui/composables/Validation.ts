import type { ComputedRef, Ref } from 'vue';
import { computed, ref, toValue, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';

export type AttributeKey = string;

export interface UseValidation {
    errors: Ref<Record<AttributeKey, string[]>>;
    isValid: Ref<boolean>;
    showErrors: Ref<boolean>;
    disableSubmit: ComputedRef<boolean>;
    validate(): void;
    reset(): void;
}

export function useValidation<T>(t: T | Ref<T> | ComputedRef<T>, validationFunction: (t: T) => Record<string, string[]>): UseValidation {
    const i18n = useI18n();

    const showErrors = ref<boolean>(false);
    const errors = ref<Record<AttributeKey, string[]>>({});
    const isValid = ref<boolean>(true);

    watchEffect(() => validate());

    const disableSubmit = computed<boolean>(() => {
        return showErrors.value && !isValid.value;
    });

    function validate(): void {
        const translatedErrors: Record<AttributeKey, string[]> = {};
        Object.entries(validationFunction(toValue(t))).forEach(([key, value]) => {
            translatedErrors[key] = value.map((rawError) => {
                const parts = rawError.split(':');
                const i18nKey = parts[0];
                const i18nParams = parts[1]?.split(',') ?? [];
                try {
                    return i18n.t(i18nKey, i18nParams);
                } catch (e) {
                    console.warn(e);
                    // test configuration will throw errors if i18n keys cannot be found
                    // use the raw error in that case, as some validators are not yet translated
                    return rawError;
                }
            });
        });
        errors.value = translatedErrors;
        const errorCount = Object.keys(errors.value).length;
        isValid.value = errorCount === 0;
        if (isValid.value) {
            console.log('✅ All user input is valid');
        } else {
            console.log(`⚠️ Found ${errorCount} validation violations`);
        }
    }

    function reset(): void {
        errors.value = {};
        isValid.value = true;
        showErrors.value = false;
        validate();
    }

    return { errors, isValid, showErrors, disableSubmit, validate, reset };
}
