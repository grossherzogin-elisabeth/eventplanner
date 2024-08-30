export class Timer {
    /**
     * waits specified amount of milliseconds and returns a preomise when done
     * @param millis milliseconds to wait
     */
    public static async wait(millis: number): Promise<void> {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve();
            }, millis);
        });
    }
}
