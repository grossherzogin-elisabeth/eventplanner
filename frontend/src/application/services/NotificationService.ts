export class NotificationService {
    constructor() {
        console.log('🚀 Initializing NotificationService');
    }

    private notificationHandler: (message: string, type: 'success' | 'warning' | 'error' | 'info') => void = (message) => {
        alert(message);
    };

    public registerNotificationHandler(handler: (message: string, type: 'success' | 'warning' | 'error' | 'info') => void): void {
        this.notificationHandler = handler;
    }

    public success(message: string): void {
        if (this.notificationHandler) {
            this.notificationHandler(message, 'success');
        }
    }

    public info(message: string): void {
        if (this.notificationHandler) {
            this.notificationHandler(message, 'info');
        }
    }

    public warning(message: string): void {
        if (this.notificationHandler) {
            this.notificationHandler(message, 'warning');
        }
    }

    public error(message: string): void {
        if (this.notificationHandler) {
            this.notificationHandler(message, 'error');
        }
    }
}
