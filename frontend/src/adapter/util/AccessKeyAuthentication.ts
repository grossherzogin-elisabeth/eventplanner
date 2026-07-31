export function appendAccessKey(uri: string): string {
    const accessKey = new URLSearchParams(location.search).get('accessKey');
    if (accessKey && uri.includes('?')) {
        return `${uri}&accessKey=${accessKey}`;
    } else if (accessKey) {
        return `${uri}?accessKey=${accessKey}`;
    }
    return uri;
}
