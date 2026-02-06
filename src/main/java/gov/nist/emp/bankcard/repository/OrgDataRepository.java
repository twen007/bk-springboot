package gov.nist.emp.bankcard.repository;

import gov.nist.emp.bankcard.entity.NistOu;
import gov.nist.emp.bankcard.entity.NistDivision;
import gov.nist.emp.bankcard.entity.NistGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

@Repository
public interface OrgDataRepository extends JpaRepository<NistOu, Long> {
	// NIST_OU
	@Query("SELECT o FROM NistOu o WHERE o.activeYn = :activeYn")
	List<NistOu> findOuByActiveYn(String activeYn);

	@Query("SELECT o FROM NistOu o WHERE o.orgId = :orgId")
	Optional<NistOu> findByOuId(@Param("orgId") Long orgId);

	@Query("SELECT o FROM NistOu o")
	List<NistOu> findAllOus();

	// NIST_DIVISION
	@Query("SELECT d FROM NistDivision d")
	List<NistDivision> findAllDivisions();

	@Query("SELECT d FROM NistDivision d WHERE d.activeYn = :activeYn")
	List<NistDivision> findDivisionsByActiveYn(String activeYn);

	// NIST_GROUP
	@Query("SELECT g FROM NistGroup g")
	List<NistGroup> findAllGroups();

	@Query("SELECT g FROM NistGroup g WHERE g.activeYn = :activeYn")
	List<NistGroup> findGroupsByActiveYn(String activeYn);

	// Find division and group by orgId
	@Query("SELECT d FROM NistDivision d WHERE d.orgId = :orgId")
	Optional<NistDivision> findDivisionByOrgId(@Param("orgId") Long orgId);

	@Query("SELECT g FROM NistGroup g WHERE g.orgId = :orgId")
	Optional<NistGroup> findGroupByOrgId(@Param("orgId") Long orgId);
}
