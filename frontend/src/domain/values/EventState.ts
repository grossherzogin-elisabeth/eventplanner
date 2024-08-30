export enum EventState {
    /**
     * event was created, but is not yet open for user signup
     */
    Draft = 'draft',
    /**
     * users can sign up for this event, the event team is not planned yet
     */
    OpenForSignup = 'open-for-signup',
    /**
     * event team is planned, team members can still sign up to the waiting list
     */
    Planned = 'planned',
}

// Draft: only visible for Eventplanners
// OpenForSignup: visible to everyone, crew only visible to Teamplanners, waitinglist visible for everyone
// Planned: visible for everyone, crew visible for everyone, waitinglist visible for everyone

// edgecase:
// Disabled: visible to everyone, signup not possible
// Cancelled: visible for everyone ?
