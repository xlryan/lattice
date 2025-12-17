package com.lattice.pro.user;

import com.lattice.core.tenancy.TenantContext;
import com.lattice.core.user.UserAccount;
import com.lattice.core.user.UserAccountRepository;
import com.lattice.pro.license.LicenseInvalidException;
import com.lattice.pro.license.LicenseManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages household members for the family edition.
 */
@Service
public class FamilyService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final LicenseManager licenseManager;

    public FamilyService(UserAccountRepository userAccountRepository,
                         PasswordEncoder passwordEncoder,
                         LicenseManager licenseManager) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.licenseManager = licenseManager;
    }

    /**
     * Creates a new family member under the caller's tenant.
     */
    @Transactional
    public void createFamilyMember(String username, String rawPassword) {
        String tenantId = TenantContext.getTenantId();
        long currentMembers = userAccountRepository.countByTenantId(tenantId);
        int maxUsers = licenseManager.resolveMaxUsers();
        if (currentMembers >= maxUsers) {
            throw new LicenseInvalidException("Family member limit exceeded");
        }
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setTenantId(tenantId);
        account.setEnabled(true);
        userAccountRepository.save(account);
    }
}
