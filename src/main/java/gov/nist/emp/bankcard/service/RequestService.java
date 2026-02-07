package gov.nist.emp.bankcard.service;

import gov.nist.emp.bankcard.entity.Request;
import gov.nist.emp.bankcard.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Request-related business logic.
 */
@Service
@Transactional
public class RequestService {

    private final RequestRepository requestRepository;

    public RequestService(RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public Optional<Request> findById(Integer requestId) {
        return requestRepository.findById(requestId);
    }

    public List<Request> findSavedByPeopleId(Integer peopleId) {
        return requestRepository.findSavedByPeopleId(peopleId);
    }

    public List<Request> findSubmittedByPeopleId(Integer peopleId) {
        return requestRepository.findSubmittedByPeopleId(peopleId);
    }

    public List<Request> findByFiscalYear(Integer fy) {
        return requestRepository.findByFiscalYear(fy);
    }

    public Request save(Request request) {
        return requestRepository.save(request);
    }

    public void deleteById(Integer requestId) {
        requestRepository.deleteById(requestId);
    }

    // TODO: Add methods for:
    // - findPendingRequests
    // - findPreparedRequests
    // - findProcessedRequests
    // - findArchivedRequests
    // - generateAuditReport
    // - generateSummaryReport
    // - submitRequest
    // - copyRequest
}
