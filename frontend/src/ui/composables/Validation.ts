import type { ComputedRef, Ref } from 'vue';
import { computed, ref, toValue, watchEffect } from 'vue';

export interface UseValidation {
    errors: Ref<Record<string, string[]>>;
    isValid: Ref<boolean>;
    showErrors: Ref<boolean>;
    disableSubmit: ComputedRef<boolean>;
    validate(): void;
    reset(): void;
}

export function useValidation<T>(t: T | Ref<T> | ComputedRef<T>, validationFunction: (t: T) => Record<string, string[]>): UseValidation {
    const showErrors = ref<boolean>(false);
    const errors = ref<Record<string, string[]>>({});
    const isValid = ref<boolean>(true);

    watchEffect(() => validate());

    const disableSubmit = computed<boolean>(() => {
        return showErrors.value && !isValid.value;
    });

    function validate(): void {
        errors.value = validationFunction(toValue(t));
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
