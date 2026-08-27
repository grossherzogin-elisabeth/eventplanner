import { getAccessKeyHeader } from '@/adapter/rest/util/getAccessKeyHeader';
import { getCsrfTokenHeader } from '@/adapter/rest/util/getCsrfTokenHeader';
import type { ErrorReportingRepository } from '@/application';

export class ErrorReportingRestRepository implements ErrorReportingRepository {
    async report(message: string, component?: string, stacktrace?: string): Promise<void> {
        try {
            await fetch('/api/v1/report/error', {
                method: 'POST',
                credentials: 'include',
                body: JSON.stringify({
                    url: location.href,
                    message: message,
                    component: component,
                    stacktrace: stacktrace,
                }),
                headers: {
                    'Content-Type': 'application/json',
                    ...getAccessKeyHeader(),
                    ...getCsrfTokenHeader(),
                },
            });
        } catch (e) {
            console.error('Failed to report error', e);
        }
    }
}
