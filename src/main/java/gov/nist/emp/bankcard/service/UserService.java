package gov.nist.emp.bankcard.service;

import gov.nist.emp.bankcard.entity.User;
import gov.nist.emp.bankcard.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for User-related business logic.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Integer peopleId) {
        return userRepository.findById(peopleId);
    }

    public List<User> findByOuId(Integer ouId) {
        return userRepository.findByOuId(ouId);
    }

    public List<User> findSupervisors() {
        return userRepository.findSupervisors();
    }

    public List<User> findActiveByOuId(Integer ouId) {
        return userRepository.findActiveByOuId(ouId);
    }

    public List<User> findActiveByDivisionCode(String divisionCode) {
        return userRepository.findActiveByDivisionCode(divisionCode);
    }

    // TODO: Add methods for:
    // - getProfile (current user with privileges)
    // - getBankcardHolders
    // - getBankcardApprovingOfficials
    // - getFundsCertifyingOfficials
    // - getReviewers
    // - getUserRoles (from NIST Org API)
}
