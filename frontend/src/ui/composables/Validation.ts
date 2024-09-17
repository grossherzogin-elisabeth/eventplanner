import type { Ref } from 'vue';
import { computed, ref } from 'vue';
import type { ValidationHint } from '@/domain';

export function useValidation<T>(t: Ref<T>, validationFunction: (t: T) => Record<string, ValidationHint[]>) {
    const showErrors = ref<boolean>(false);

    const errors = computed<Record<string, ValidationHint[]>>(() => {
        return validationFunction(t.value);
    });

    const isValid = computed<boolean>(() => {
        return Object.keys(errors.value).length === 0;
    });

    const disableSubmit = computed<boolean>(() => {
        return showErrors.value && !isValid.value;
    });

    function reset(): void {
        showErrors.value = false;
    }

    return { errors, isValid, showErrors, disableSubmit, reset };
}
