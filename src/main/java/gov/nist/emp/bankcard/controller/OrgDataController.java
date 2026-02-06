package gov.nist.emp.bankcard.controller;

import gov.nist.emp.bankcard.entity.NistDivision;
import gov.nist.emp.bankcard.dto.DivisionDto;
import gov.nist.emp.bankcard.entity.NistGroup;
import gov.nist.emp.bankcard.dto.GroupDto;
import gov.nist.emp.bankcard.entity.NistOu;
import gov.nist.emp.bankcard.dto.OuDto;
import gov.nist.emp.bankcard.repository.OrgDataRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/org-data")
public class OrgDataController {
    private final OrgDataRepository orgDataRepository;

    public OrgDataController(OrgDataRepository orgDataRepository) {
        this.orgDataRepository = orgDataRepository;
    }

    // --- NIST_OU APIs ---
    @GetMapping("/ous")
    public java.util.List<OuDto> getAllNistOus() {
        return orgDataRepository.findAllOus().stream().map(this::toOuDto).toList();
    }


    @GetMapping("/ous/{orgId}")
    public Optional<OuDto> getNistOuById(@PathVariable Long orgId) {
        return orgDataRepository.findByOuId(orgId).map(this::toOuDto);
    }


    @GetMapping("/ous/active")
    public java.util.List<OuDto> getActiveNistOus() {
        return orgDataRepository.findOuByActiveYn("Y").stream().map(this::toOuDto).toList();
    }
    private OuDto toOuDto(NistOu ou) {
        return new OuDto(
            ou.getOrgId(),
            null, // divisionId
            null, // groupId
            ou.getOrgName(),
            ou.getOrgCd(),
            ou.getOrgAcrnm(),
            ou.getOrgCd() + (ou.getOrgAcrnm() != null ? ("-" + ou.getOrgAcrnm()) : "")
        );
    }

     // --- NIST_DIVISION APIs ---
    @GetMapping("/divisions")
    public java.util.List<DivisionDto> getAllDivisions() {
        return orgDataRepository.findAllDivisions().stream().map(this::toDivisionDto).toList();
    }


    @GetMapping("/divisions/{orgId}")
    public Optional<DivisionDto> getDivisionByOrgId(@PathVariable Long orgId) {
        return orgDataRepository.findDivisionByOrgId(orgId).map(this::toDivisionDto);
    }


    @GetMapping("/divisions/active")
    public java.util.List<DivisionDto> getActiveDivisions() {
        return orgDataRepository.findDivisionsByActiveYn("Y").stream().map(this::toDivisionDto).toList();
    }
    private DivisionDto toDivisionDto(NistDivision div) {
        return new DivisionDto(
            null, // ouId
            div.getOrgId(),
            null, // groupId
            div.getOrgName(),
            div.getOrgCd(),
            div.getOrgAcrnm(),
            div.getOrgCd() + (div.getOrgAcrnm() != null ? ("-" + div.getOrgAcrnm()) : "")
        );
    }

    // --- NIST_GROUP APIs ---
    @GetMapping("/groups")
    public java.util.List<GroupDto> getAllGroups() {
        return orgDataRepository.findAllGroups().stream().map(this::toGroupDto).toList();
    }


    @GetMapping("/groups/{orgId}")
    public Optional<GroupDto> getGroupByOrgId(@PathVariable Long orgId) {
        return orgDataRepository.findGroupByOrgId(orgId).map(this::toGroupDto);
    }


    @GetMapping("/groups/active")
    public java.util.List<GroupDto> getActiveGroups() {
        return orgDataRepository.findGroupsByActiveYn("Y").stream().map(this::toGroupDto).toList();
    }
    private GroupDto toGroupDto(NistGroup group) {
        return new GroupDto(
            group.getOuOrgId(),
            group.getDivOrgId(),
            group.getOrgId(),
            group.getOrgName(),
            group.getOrgCd(),
            group.getOrgAcrnm(),
            group.getOrgCd() != null && group.getOrgCd().contains(".") ? group.getOrgCd() : group.getOrgCd()
        );
    }
}
