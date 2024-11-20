export interface Location {
    order: number;
    name: string;
    icon: string;
    address?: string;
    addressLink?: string;
    information?: string;
    informationLink?: string;
    country?: string;
    eta?: Date;
    etd?: Date;
}
