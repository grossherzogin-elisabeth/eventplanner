import type { ComputedRef, Ref } from 'vue';
import { computed, ref, toValue, watchEffect } from 'vue';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export function useValidation<T>(t: T | Ref<T> | ComputedRef<T>, validationFunction: (t: T) => Record<string, string[]>) {
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
