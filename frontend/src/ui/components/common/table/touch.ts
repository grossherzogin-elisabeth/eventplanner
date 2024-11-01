export function useLongTouch(longTouchDelay: number = 500) {
    let touchStartEvent: TouchEvent | undefined = undefined;
    let touchMoveEvent: TouchEvent | undefined = undefined;
    let longTouchTimeout: ReturnType<typeof setTimeout> | undefined = undefined;
    let longTouchFired: boolean | undefined = undefined;

    function start(event: TouchEvent): Promise<void> {
        // clearTimeout(longTouchTimeout);
        touchStartEvent = event;
        touchMoveEvent = event;
        return new Promise<void>((resolve, reject) => {
            longTouchTimeout = setTimeout(() => {
                if (!touchMoveEvent || !touchMoveEvent) {
                    longTouchFired = false;
                    reject();
                    return;
                }
                const startTouchPoint = touchStartEvent.changedTouches.item(0);
                const endTouchPoint = touchMoveEvent.changedTouches.item(0);
                touchStartEvent = undefined;
                touchMoveEvent = undefined;
                if (!startTouchPoint || !endTouchPoint) {
                    longTouchFired = false;
                    reject();
                    return;
                }
                const diffX = Math.abs(startTouchPoint.clientX - endTouchPoint.clientX);
                const diffY = Math.abs(startTouchPoint.clientY - endTouchPoint.clientY);
                if (diffX < 10 && diffY < 10) {
                    longTouchFired = true;
                    resolve();
                    return;
                }
                longTouchFired = false;
                reject();
                return;
            }, longTouchDelay);
        });
    }

    function update(event: TouchEvent): void {
        touchMoveEvent = event;
    }

    function end(): void {
        touchStartEvent = undefined;
        touchMoveEvent = undefined;
        clearTimeout(longTouchTimeout);
        if (longTouchFired === undefined) {
            longTouchFired = false;
        }
        setTimeout(() => {
            longTouchFired = undefined;
        }, 50);
    }

    function isLongTouch(): boolean {
        return longTouchFired === true;
    }

    function isTouch(): boolean {
        return longTouchFired !== undefined;
    }

    return { start, update, end, isLongTouch, isTouch };
}
