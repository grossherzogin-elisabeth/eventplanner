/**
 * waits specified amount of milliseconds and returns a preomise when done
 * @param millis milliseconds to wait
 */
export async function wait(millis: number): Promise<void> {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve();
        }, millis);
    });
}
