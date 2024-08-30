import type { User } from '@/domain';

export class UserService {
    public doesUserMatchFilter(user: User, filter: string): boolean {
        const filterLc = filter.toLowerCase();
        const fullname = `${user.firstName} ${user.lastName}`;
        if (fullname.toLowerCase().includes(filterLc)) {
            return true;
        }
        return false;
    }
}
