package com.tus.maintainx.service;

import com.tus.maintainx.dto.NetworkElementCreateDTO;
import com.tus.maintainx.dto.NetworkElementResponseDTO;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.enums.AuditAction;
import com.tus.maintainx.enums.AuditEntityType;
import com.tus.maintainx.repository.NetworkElementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NetworkElementService {

    private final NetworkElementRepository networkElementRepository;
    private final AuditService auditService;

    public NetworkElementResponseDTO create(NetworkElementCreateDTO dto) {

        NetworkElementEntity e = new NetworkElementEntity();
        e.setElementCode(buildPendingCode());
        e.setName(dto.getName().trim());
        e.setElementType(dto.getElementType().trim());
        e.setRegion(dto.getRegion().trim());
        e.setStatus(dto.getStatus());

        NetworkElementEntity saved = networkElementRepository.save(e);
        saved.setElementCode(buildElementCode(saved.getId()));
        saved = networkElementRepository.save(saved);

        auditService.log(
                AuditEntityType.NETWORK_ELEMENT,
                saved.getId(),
                AuditAction.CREATED,
                "Network element created: " + saved.getElementCode()
        );
        log.info("Network element {} created", saved.getElementCode());

        return toDto(saved);
    }

    private NetworkElementResponseDTO toDto(NetworkElementEntity networkElementEntity) {
        return new NetworkElementResponseDTO(
                networkElementEntity.getId(),
                networkElementEntity.getElementCode(),
                networkElementEntity.getName(),
                networkElementEntity.getElementType(),
                networkElementEntity.getRegion(),
                networkElementEntity.getStatus()
        );
    }

    public List<NetworkElementResponseDTO> getAll() {
        return networkElementRepository.findAll().stream().map(this::toDto).toList();
    }

    private NetworkElementEntity getById(Long id) {
        return networkElementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Network element not found with id=" + id
                ));
    }

    public NetworkElementResponseDTO update(Long id, NetworkElementCreateDTO dto) {

        NetworkElementEntity existing = getById(id);

        existing.setName(dto.getName().trim());
        existing.setElementType(dto.getElementType().trim());
        existing.setRegion(dto.getRegion().trim());
        existing.setStatus(dto.getStatus());

        NetworkElementEntity saved = networkElementRepository.save(existing);

        auditService.log(
                AuditEntityType.NETWORK_ELEMENT,
                saved.getId(),
                AuditAction.UPDATED,
                "Network element updated: " + saved.getElementCode()
        );
        log.info("Network element {} updated", saved.getElementCode());

        return toDto(saved);
    }

    public NetworkElementResponseDTO deactivate(Long id) {
        NetworkElementEntity existing = getById(id);

        existing.setStatus("DEACTIVE");

        NetworkElementEntity saved = networkElementRepository.save(existing);

        auditService.log(
                AuditEntityType.NETWORK_ELEMENT,
                saved.getId(),
                AuditAction.DEACTIVATED,
                "Network element deactivated: " + saved.getElementCode()
        );
        log.info("Network element {} deactivated", saved.getElementCode());

        return toDto(saved);
    }

    public NetworkElementResponseDTO activate(Long id) {
        NetworkElementEntity existing = getById(id);

        existing.setStatus("ACTIVE");

        NetworkElementEntity saved = networkElementRepository.save(existing);

        auditService.log(
                AuditEntityType.NETWORK_ELEMENT,
                saved.getId(),
                AuditAction.ACTIVATED,
                "Network element activated: " + saved.getElementCode()
        );
        log.info("Network element {} activated", saved.getElementCode());

        return toDto(saved);
    }

    public void delete(Long id) {
        NetworkElementEntity existing = getById(id);

        auditService.log(
                AuditEntityType.NETWORK_ELEMENT,
                existing.getId(),
                AuditAction.DELETED,
                "Network element deleted: " + existing.getElementCode()
        );
        log.info("Network element {} deleted", existing.getElementCode());

        networkElementRepository.deleteById(id);
    }

    public NetworkElementResponseDTO getByElementId(Long id) {
        return toDto(getById(id));
    }

    private String buildElementCode(Long id) {
        return String.format("NE-%03d", id);
    }

    private String buildPendingCode() {
        return "PENDING-" + System.nanoTime();
    }
}
