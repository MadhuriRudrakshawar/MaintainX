package com.tus.maintainx.service;

import com.tus.maintainx.dto.NetworkElementCreateDTO;
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

    public NetworkElementEntity create(NetworkElementCreateDTO dto){

        NetworkElementEntity e = new NetworkElementEntity();
        e.setElementCode(dto.getElementCode().trim());
        e.setName(dto.getName().trim());
        e.setElementType(dto.getElementType().trim());
        e.setRegion(dto.getRegion().trim());
        e.setStatus(dto.getStatus());

        return networkElementRepository.save(e);

    }

    public List<NetworkElementEntity> getAll(){
        return networkElementRepository.findAll();
    }

    public NetworkElementEntity getById(Long id){
        return networkElementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Network element not found with id=" + id
                ));

    }

    public NetworkElementEntity update(Long id, NetworkElementCreateDTO dto){

        NetworkElementEntity existing = getById(id);

        existing.setElementCode(dto.getElementCode().trim());
        existing.setName(dto.getName().trim());
        existing.setElementType(dto.getElementType().trim());
        existing.setRegion(dto.getRegion().trim());
        existing.setStatus(dto.getStatus());
        return networkElementRepository.save(existing);

    }

    public NetworkElementEntity deactivate(Long id){
        NetworkElementEntity existing = getById(id);

        existing.setStatus("DEACTIVE");

        return networkElementRepository.save(existing);

    }

    public NetworkElementEntity activate(Long id){
        NetworkElementEntity existing = getById(id);

        existing.setStatus("ACTIVE");

        return networkElementRepository.save(existing);

    }

}
