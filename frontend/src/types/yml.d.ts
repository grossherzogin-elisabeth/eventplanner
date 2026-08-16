declare module '*.yml' {
    const value: Record<string, unknown>;
    export default value;
}

declare module '*.yaml' {
    const value: Record<string, unknown>;
    export default value;
}
