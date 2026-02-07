package gov.nist.emp.bankcard.service;

import gov.nist.emp.bankcard.entity.Route;
import gov.nist.emp.bankcard.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Route-related business logic (approval workflow).
 */
@Service
@Transactional
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public List<Route> findByRequestId(Integer requestId) {
        return routeRepository.findByRequestId(requestId);
    }

    public Optional<Route> findCurrentRouteByRequestId(Integer requestId) {
        return routeRepository.findCurrentRouteByRequestId(requestId);
    }

    public List<Route> findPendingByApproverId(Integer approverId) {
        return routeRepository.findPendingByApproverId(approverId);
    }

    public Route save(Route route) {
        return routeRepository.save(route);
    }

    // TODO: Add methods for:
    // - approveRequest
    // - rejectRequest
    // - pullBackRequest
    // - rerouteRequest
    // - getApprovalHistory
}
