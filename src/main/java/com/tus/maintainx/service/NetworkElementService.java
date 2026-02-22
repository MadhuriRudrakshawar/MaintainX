package com.tus.maintainx.service;

import com.tus.maintainx.dto.NetworkElementCreateDTO;
import com.tus.maintainx.dto.NetworkElementResponseDTO;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.repository.NetworkElementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NetworkElementService {

    private final NetworkElementRepository networkElementRepository;

    public NetworkElementResponseDTO create(NetworkElementCreateDTO dto) {

        NetworkElementEntity e = new NetworkElementEntity();
        e.setElementCode(dto.getElementCode().trim());
        e.setName(dto.getName().trim());
        e.setElementType(dto.getElementType().trim());
        e.setRegion(dto.getRegion().trim());
        e.setStatus(dto.getStatus());

        NetworkElementEntity networkElementEntity = networkElementRepository.save(e);

        return toDto(networkElementEntity);

    }

    private NetworkElementResponseDTO toDto(NetworkElementEntity networkElementEntity) {
        return new NetworkElementResponseDTO(
                networkElementEntity.getId(), networkElementEntity.getElementCode(),
                networkElementEntity.getName(), networkElementEntity.getElementType(),
                networkElementEntity.getRegion(), networkElementEntity.getStatus());
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

        existing.setElementCode(dto.getElementCode().trim());
        existing.setName(dto.getName().trim());
        existing.setElementType(dto.getElementType().trim());
        existing.setRegion(dto.getRegion().trim());
        existing.setStatus(dto.getStatus());
        return toDto(networkElementRepository.save(existing));

    }

    public NetworkElementResponseDTO deactivate(Long id) {
        NetworkElementEntity existing = getById(id);

        existing.setStatus("DEACTIVE");

        return toDto(networkElementRepository.save(existing));

    }

    public NetworkElementResponseDTO activate(Long id) {
        NetworkElementEntity existing = getById(id);

        existing.setStatus("ACTIVE");

        return toDto(networkElementRepository.save(existing));

    }

    public void delete(Long id) {
        if (!networkElementRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Network element not found with id=" + id
            );
        }
        networkElementRepository.deleteById(id);
    }

    public NetworkElementResponseDTO getByElementId(Long id) {
        return toDto(getById(id));
    }
}
