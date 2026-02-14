package com.tus.maintainx.service;

import com.tus.maintainx.dto.NetworkElementCreateDTO;
import com.tus.maintainx.entity.NetworkElementEntity;
import com.tus.maintainx.repository.NetworkElementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}
