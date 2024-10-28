export function filterDuplicates<T>(value: T, index: number, array: T[]): boolean {
    return array.indexOf(value) === index;
}

export function filterUndefined<T>(item: T | undefined): item is T {
    return !!item;
}
