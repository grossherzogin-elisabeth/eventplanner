export interface ErrorDialogMessage {
    title?: string;
    message?: string;
    error?: Error | Response;
    retry?: () => void;
    retryText?: string;
    cancelText?: string;
}
