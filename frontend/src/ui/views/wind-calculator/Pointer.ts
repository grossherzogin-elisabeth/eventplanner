export interface Point {
    x: number;
    y: number;
}

export interface Pointer {
    x1: number;
    y1: number;
    x2: number;
    y2: number;
}

export function calculatePointer(from: Point, a: number, l: number): Pointer {
    const w = (a - 90) * (Math.PI / 180);
    const x2 = from.x + Math.cos(w) * l;
    const y2 = from.y + Math.sin(w) * l;
    return {
        x1: from.x,
        y1: from.y,
        x2: x2,
        y2: y2,
    };
}

export function calculatePointerDirection(pointer: Pointer): number {
    const x = pointer.x2 - pointer.x1;
    const y = pointer.y2 - pointer.y1;
    return Math.round(Math.atan2(y, x) * (180 / Math.PI) + 90);
}
