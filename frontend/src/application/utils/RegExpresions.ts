export const PHONE_REGEX = new RegExp('^[0-9+\\- ]+$');
export const NAME_REGEX = new RegExp(
    "^[a-zA-ZàáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžæÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð ,.'-]+$"
);
export const EMAIL_REGEX = new RegExp('^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,8}$');
