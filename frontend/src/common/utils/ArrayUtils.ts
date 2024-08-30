export class ArrayUtils {
    public static filterDuplicates<T>(value: T, index: number, array: T[]): boolean {
        return array.indexOf(value) === index;
    }

    public static filterUndefined<T>(item: T | undefined): item is T {
        return !!item;
    }
}
