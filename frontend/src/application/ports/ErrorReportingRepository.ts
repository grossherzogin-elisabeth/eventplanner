export interface ErrorReportingRepository {
    report(message: string, component?: string, stacktrace?: string): Promise<void>;
}
