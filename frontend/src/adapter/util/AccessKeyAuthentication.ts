/**
 * @deprecated Use getAccessKeyHeader() instead to add the access key as a header.
 */
export function appendAccessKey(uri: string): string {
    return uri;
}

export function getAccessKeyHeader(): Record<string, string> {
    const accessKey = new URLSearchParams(location.search).get('accessKey');
    if (accessKey) {
        return { 'Access-Key': accessKey };
    }
    return {};
}
