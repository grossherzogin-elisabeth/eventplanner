import type { DetachedWindowAPI } from 'happy-dom';

export function happyDOM(): DetachedWindowAPI {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return (window as any).happyDOM;
}
