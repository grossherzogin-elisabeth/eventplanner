export function filterDuplicates<T>(value: T, index: number, array: T[]): boolean {
    return array.indexOf(value) === index;
}

export function filterUndefined<T>(item: T | undefined): item is T {
    return !!item;
}

export function hasAnyOverlap<T>(a: T[], b: T[]): boolean {
    return a.find((it) => b.includes(it)) !== undefined;
}
