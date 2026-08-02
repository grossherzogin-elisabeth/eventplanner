export function getAccessKeyHeader(): Record<string, string> {
    const accessKey = new URLSearchParams(location.search).get('accessKey');
    if (accessKey) {
        return { 'Access-Key': accessKey };
    }
    return {};
}
