export interface ErrorDialogMessage {
    title?: string;
    message?: string;
    error?: unknown | Error | Response;
    retry?: () => void;
}
