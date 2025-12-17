package com.lattice.pro.user;

import com.lattice.core.user.UserLimitService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Commercial override that removes project limits.
 */
@Service
@Primary
public class UnlimitedUserService implements UserLimitService {

    @Override
    public int getMaxProjects() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isLimitReached(long userId, int currentProjectCount) {
        return false;
    }
}
