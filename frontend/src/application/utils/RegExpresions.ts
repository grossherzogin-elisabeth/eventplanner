export const PHONE_REGEX = new RegExp(String.raw`^[0-9+\- ]+$`);
export const NAME_REGEX = new RegExp(
    '^[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžæÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð ,.\'-]+$'
);
export const EMAIL_REGEX = new RegExp(String.raw`^[\w\-\.]+@([\w\-]+\.)+[\w\-]{2,8}$`);
export const PASS_NR_REGEX = new RegExp('^[0-9A-Z ]+$');
export const NUMBER_REGEX = new RegExp(String.raw`^\d*$`);

const DD = '(0?[1-9]|[12][0-9]|3[01])';
const MM = '(0?[1-9]|1[012])';
const YY = String.raw`\d{2}`;
const YYYY = String.raw`\d{4}`;
const DELIMITER = '[- /.]';
export const DATE_DD_MM_YYYY_REGEX = new RegExp(`^${DD}${DELIMITER}${MM}${DELIMITER}${YYYY}$`);
export const DATE_DD_MM_YY_REGEX = new RegExp(`^${DD}${DELIMITER}${MM}${DELIMITER}${YY}$`);
export const DATE_MM_DD_YYYY_REGEX = new RegExp(`^${MM}${DELIMITER}${DD}${DELIMITER}${YYYY}$`);
export const DATE_YYYY_MM_DD_REGEX = new RegExp(`^${YYYY}${DELIMITER}${MM}${DELIMITER}${DD}$`);

