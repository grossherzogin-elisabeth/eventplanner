import type { ComputedRef, Ref } from 'vue';
import { watchEffect } from 'vue';
import { toValue } from 'vue';
import { computed, ref } from 'vue';

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
        isValid.value = Object.keys(errors.value).length === 0;
    }

    function reset(): void {
        errors.value = {};
        isValid.value = true;
        showErrors.value = false;
        validate();
    }

    return { errors, isValid, showErrors, disableSubmit, validate, reset };
}
