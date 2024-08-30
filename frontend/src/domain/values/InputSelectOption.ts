export interface InputSelectOption<T = string> {
    label: string;
    value: T;
    hidden?: boolean;
}
