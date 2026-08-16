declare const value: {
    components: {
        'error-dialog': {
            'default-text': string;
            'default-title': string;
            'details': string;
            'retry': string;
            'show-details': string;
        };
        'event-cancel-dialog': {
            message: string;
            submit: string;
            title: string;
        };
        'event-details-card': {
            assigned: string;
            days: string;
            registrations: string;
            title: string;
            waitinglist: string;
        };
        'event-details-sheet': {
            'note-assigned': string;
            'note-missing-crew': string;
            'note-on-waiting-list': string;
            'show-details': string;
        };
        'event-locations-card': {
            'address-link': string;
            'eta': string;
            'etd': string;
            'learn-more': string;
            'placeholder': string;
            'title': string;
        };
        'event-participants-card': {
            'assigned': string;
            'empty': string;
            'no-registrations': string;
            'no-waitinglist': string;
            'placeholder': string;
            'placeholder-title': string;
            'placeholder-title-f': string;
            'placeholder-title-m': string;
            'placeholder-waiting-list-hint': string;
            'registrations': string;
            'unknown': string;
            'waitinglist': string;
        };
        'event-registration-details-card': {
            'arrival': string;
            'arrival-description': string;
            'arrival-note': string;
            'note': string;
            'note-description': string;
            'note-none': string;
            'overnight-stay': string;
            'overnight-stay-description': string;
            'overnight-stay-note': string;
            'position': string;
            'position-description': string;
            'title': string;
        };
        'menu': {
            'account': string;
            'calendar': string;
            'events': string;
            'events-admin': string;
            'feedback': string;
            'home': string;
            'impersonate': string;
            'no-account': {
                header: string;
                login: string;
                message: string;
                register: string;
            };
            'settings': string;
            'sign-out': string;
            'start': string;
            'users': string;
            'users-admin': string;
        };
    };
    domain: {
        'address': {
            'address-line-1': string;
            'address-line-2': string;
            'country': string;
            'town': string;
            'zipcode': string;
        };
        'emergency-contact': {
            name: string;
            phone: string;
        };
        'event': {
            'actions': {
                'add-to-crew': string;
                'cancel': string;
                'contact-crew': string;
                'create-calendar-entry': string;
                'create-registration': string;
                'delete': string;
                'edit': string;
                'export-to-template': string;
                'leave-waiting-list': string;
                'remove-from-crew': string;
                'sign-up': string;
                'update-state-to-canceled': string;
                'update-state-to-open-for-signup': string;
                'update-state-to-published': string;
                'view': string;
            };
            'category': string;
            'crew': string;
            'crew-count': string;
            'description': string;
            'duration': string;
            'end': string;
            'end-date': string;
            'end-time': string;
            'name': string;
            'no-slot-for-position-error': {
                message: string;
                retry: string;
                title: string;
            };
            'registration-count': string;
            'registrations': string;
            'route': string;
            'signup-type': string;
            'slot': {
                empty: string;
            };
            'start': string;
            'start-date': string;
            'start-time': string;
            'status': string;
            'template': string;
            'time': string;
            'type': string;
            'waiting-list': string;
        };
        'event-signup-type': {
            assignment: string;
            open: string;
        };
        'event-slot': {
            'alternative-positions': string;
            'criticality': string;
            'display-name': string;
            'important': string;
            'optional': string;
            'position': string;
            'required': string;
        };
        'event-state': {
            'canceled': string;
            'crew-wanted': string;
            'draft': string;
            'full': string;
            'open-for-signup': string;
            'open-slots': string;
            'planned': string;
        };
        'event-type': {
            'multi-day-event': string;
            'other': string;
            'single-day-event': string;
            'training-event': string;
            'weekend-event': string;
            'work-event': string;
        };
        'location': {
            'address': string;
            'address-link': string;
            'eda': string;
            'edd': string;
            'eta': string;
            'etd': string;
            'icon': string;
            'information': string;
            'information-link': string;
            'name': string;
        };
        'registration': {
            'arrival': string;
            'arrival-on-day-before': string;
            'confirmation-pending': string;
            'confirmed': string;
            'guest': string;
            'name': string;
            'note': string;
            'overnight-stay': string;
            'position': string;
            'unknown-user': string;
        };
        'role': {
            ROLE_ADMIN: string;
            ROLE_EVENT_LEADER: string;
            ROLE_EVENT_PLANNER: string;
            ROLE_TEAM_MEMBER: string;
            ROLE_TEAM_PLANNER: string;
            ROLE_USER_MANAGER: string;
        };
        'types': {
            event: string;
            registration: string;
            user: string;
        };
        'user': {
            'actions': {
                'create': string;
                'impersonate': string;
                'write-email': string;
            };
            'address': string;
            'date-and-place-of-birth': string;
            'date-of-birth': string;
            'diet': string;
            'diseases': string;
            'email': string;
            'emergency-contact': string;
            'first-name': string;
            'gender': string;
            'intolerances': string;
            'last-login-at': string;
            'last-name': string;
            'medication': string;
            'middle-name': string;
            'mobile': string;
            'name': string;
            'nationality': string;
            'nick-name': string;
            'no-login-recorded': string;
            'pass-nr': string;
            'phone': string;
            'phone-work': string;
            'place-of-birth': string;
            'qualification': {
                'all-valid': string;
                'expired-count': string;
            };
            'title': string;
        };
        'user-qualification': {
            'expires-at': string;
            'status': {
                'expired': string;
                'expiring-soon': string;
                'valid': string;
            };
        };
    };
    generic: {
        'add': string;
        'apply': string;
        'cancel': string;
        'close': string;
        'delete': string;
        'diet': {
            omnivore: string;
            vegan: string;
            vegetarian: string;
        };
        'discard-changes': string;
        'edit': string;
        'error': string;
        'filter-entries': string;
        'gender': {
            d: string;
            f: string;
            m: string;
        };
        'language': {
            de: string;
            en: string;
        };
        'loading': string;
        'markdown-supported': string;
        'month': {
            '0': string;
            '1': string;
            '2': string;
            '3': string;
            '4': string;
            '5': string;
            '6': string;
            '7': string;
            '8': string;
            '9': string;
            '10': string;
            '11': string;
        };
        'month-short': {
            '0': string;
            '1': string;
            '2': string;
            '3': string;
            '4': string;
            '5': string;
            '6': string;
            '7': string;
            '8': string;
            '9': string;
            '10': string;
            '11': string;
        };
        'move-down': string;
        'move-up': string;
        'no': string;
        'no-entries': string;
        'no-information': string;
        'no-matches': string;
        'ok': string;
        'optional': string;
        'please-select': string;
        'save': string;
        'select-all': string;
        'selected-count': string;
        'theme': {
            dark: string;
            light: string;
            system: string;
        };
        'validation': {
            'after': string;
            'before': string;
            'invalid': string;
            'invalid-date': string;
            'invalid-email': string;
            'invalid-name': string;
            'invalid-phone-number': string;
            'max-length': string;
            'min-length': string;
            'must-not-include-lastname': string;
            'required': string;
        };
        'weekday-short': {
            '0': string;
            '1': string;
            '2': string;
            '3': string;
            '4': string;
            '5': string;
            '6': string;
        };
        'yes': string;
    };
    views: {
        'account': {
            'app-features': {
                title: string;
            };
            'app-settings': {
                'language': string;
                'language-description': string;
                'notifications': string;
                'permissions': string;
                'preferred-position': string;
                'preferred-position-description': string;
                'roles': string;
                'theme': string;
                'title': string;
            };
            'contact': {
                'address-description': string;
                'email-description': string;
                'phone-description': string;
                'title': string;
            };
            'diet': {
                'diet-description': string;
                'intolerances-description': string;
                'kitchen-info': string;
                'title': string;
            };
            'emergency': {
                'click-to-show': string;
                'diseases-description': string;
                'emergency-contact-description': string;
                'medication-description': string;
                'privacy': string;
                'title': string;
            };
            'personal': {
                'date-and-place-of-birth-description': string;
                'gender-description': string;
                'hint': string;
                'name-description': string;
                'nationality-description': string;
                'nick-name-hint': string;
                'official-name-hint': string;
                'passport-description': string;
                'passport-info': string;
                'passport-link': string;
                'title': string;
            };
            'qualifications': {
                'status-expired': string;
                'status-expires-on': string;
                'status-expiring-soon': string;
                'status-no-expires': string;
                'status-valid': string;
            };
            'tab': {
                data: string;
                qualifications: string;
                settings: string;
            };
            'title': string;
        };
        'calendar': {
            'create-event': string;
            'days': string;
            'title': string;
        };
        'event-admin-list': {
            'action': {
                'add-event': string;
                'add-registration': string;
                'cancel-event': string;
                'contact-crew': string;
                'create-event': string;
                'delete-event': string;
                'edit-event': string;
                'export': string;
                'open-signup': string;
                'publish-crew': string;
                'request-more-crew': string;
                'show-event': string;
            };
            'batch-edit': {
                'copy-slots-from': string;
                'copy-slots-warning': string;
                'dont-change-slots': string;
                'info': string;
                'not-changed': string;
                'title': string;
            };
            'dialog': {
                'delete': {
                    message: string;
                    submit: string;
                    title: string;
                };
                'open-signup': {
                    cancel: string;
                    message: string;
                    submit: string;
                    title: string;
                };
                'publish-crew': {
                    cancel: string;
                    message: string;
                    submit: string;
                    title: string;
                };
            };
            'filter': {
                'all-events': string;
                'all-status': string;
                'free-slots': string;
                'search': string;
                'status': string;
                'waitinglist': string;
            };
            'state': {
                'free-slots-for': string;
                'missing-crew': string;
            };
            'tab': {
                future: string;
            };
            'tab-title': string;
            'table': {
                'day-count': string;
                'no-route': string;
                'team': string;
            };
        };
        'event-confirm-participation': {
            'canceled': {
                info: string;
                title: string;
            };
            'canceled-now': {
                info: string;
                title: string;
            };
            'confirm': string;
            'confirmed': {
                info: string;
                title: string;
            };
            'decline': string;
            'details': {
                title: string;
            };
            'info': {
                deadline: string;
                message: string;
            };
            'invalid-link': {
                info: string;
                title: string;
            };
            'no-registration': {
                info: string;
                title: string;
            };
            'not-found': {
                info: string;
                title: string;
            };
            'title': string;
        };
        'event-details': {
            'add-note': string;
            'create-calendar-entry': string;
            'edit-event': string;
            'edit-registration': string;
            'info-assigned': string;
            'info-canceled': string;
            'info-confirmed': string;
            'info-draft': string;
            'info-missing-crew': string;
            'info-planning': string;
            'info-waitinglist': string;
            'leave-crew': string;
            'leave-crew-dialog': {
                message: string;
                submit: string;
                title: string;
            };
            'leave-waitinglist': string;
            'save-calendar': string;
            'sign-up': string;
        };
        'event-edit': {
            'actions': {
                'add-location': string;
                'add-registration': string;
                'add-slot': string;
                'add-to-crew': string;
                'cancel-event': string;
                'contact-crew': string;
                'delete-eta': string;
                'delete-etd': string;
                'delete-location': string;
                'delete-registration': string;
                'delete-slot': string;
                'duplicate-slot': string;
                'edit-location': string;
                'edit-registration': string;
                'edit-slot': string;
                'edit-start-location': string;
                'move-to-waiting-list': string;
                'open-signup': string;
                'publish-crew': string;
                'reset-crew': string;
                'show-user': string;
            };
            'edit-registration': {
                'guest-title': string;
                'guest-warning': string;
                'no-qualification': string;
                'qualifications': string;
                'select-position': string;
                'select-user': string;
                'title': string;
            };
            'empty-crew': {
                desc: string;
                title: string;
            };
            'empty-waitinglist': {
                desc: string;
                title: string;
            };
            'filter': {
                'all-positions': string;
                'free-slots': string;
                'pending-confirmation': string;
                'valid-qualifications': string;
            };
            'free': string;
            'info-canceled': string;
            'info-draft': string;
            'info-missing-crew': string;
            'info-signup': string;
            'registration-id': string;
            'secure-crew': string;
            'tab': {
                crew: string;
                data: string;
                locations: string;
                registrations: string;
                slots: string;
            };
            'tooltip': {
                'guest-info': string;
                'information': string;
                'positions': string;
                'qualifications': string;
            };
            'unsaved-changes': {
                message: string;
                title: string;
            };
            'validation': {
                'missing-location-icon': string;
                'missing-location-name': string;
            };
        };
        'event-list': {
            'action': {
                'cancel': string;
                'create-calendar-entry': string;
                'leave-waitinglist': string;
                'link-event-details': string;
                'signup': string;
            };
            'filter': {
                'all-types': string;
                'assigned': string;
                'free-slots': string;
                'search': string;
                'waitinglist': string;
            };
            'note-no-position': string;
            'state': {
                assigned: string;
                waitinglist: string;
            };
            'tab': {
                future: string;
            };
            'table': {
                'assigned-as': string;
                'day-count': string;
                'on-waiting-list-as': string;
                'registration-count': string;
                'team-count': string;
            };
        };
        'home': {
            'find-next-event': string;
            'loading-events': string;
            'next-month': string;
            'no-upcoming-events-description': string;
            'no-upcoming-events-title': string;
            'tab-title': string;
            'this-month': string;
            'waitinglist': string;
        };
        'settings': {
            filter: {
                expires: string;
                positions: string;
            };
            notifications: {
                'email': string;
                'email-description': string;
                'teams-webhook-not-set-up': string;
                'teams-webhook-set-up': string;
                'teams-webhook-url': string;
                'teams-webhook-url-description': string;
                'title': string;
            };
            positions: {
                'add-new': string;
                'color': string;
                'delete-message': string;
                'delete-title': string;
                'edit': string;
                'id': string;
                'imo-list-rank': string;
                'name': string;
                'prio': string;
                'validation': {
                    'key-must-be-unique': string;
                    'name-must-be-unique': string;
                };
            };
            qualifications: {
                'add-new': string;
                'delete-message': string;
                'delete-title': string;
                'description': string;
                'edit': string;
                'expires': string;
                'icon': string;
                'icon-placeholder': string;
                'key': string;
                'name': string;
                'positions': string;
                'status-expires': string;
                'status-no-expires': string;
                'validation': {
                    'key-must-be-unique': string;
                    'name-must-be-unique': string;
                };
            };
            tab: {
                email: string;
                general: string;
                notifications: string;
                positions: string;
                qualifications: string;
            };
            ui: {
                'menu-title': string;
                'menu-title-description': string;
                'support-email': string;
                'support-email-description': string;
                'tab-title': string;
                'tab-title-description': string;
                'tech-support-email': string;
                'tech-support-email-description': string;
                'theme-color': string;
                'theme-color-description': string;
                'title': string;
            };
        };
        'user-details': {
            tab: {
                certificates: string;
                contact: string;
                data: string;
                emergency: string;
                events: string;
                other: string;
                roles: string;
            };
        };
        'user-list': {
            'filter': {
                'active-crew': string;
                'all-events': string;
                'all-positions': string;
                'expired-qualifications': string;
                'not-verified': string;
            };
            'no-position': string;
            'qualification-summary': {
                'all-valid': string;
                'expired': string;
                'expiring-soon': string;
            };
            'tab': {
                admins: string;
                members: string;
                unknown: string;
            };
        };
    };
};
export default value;
