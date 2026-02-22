package com.tus.maintainx.repository;

import com.tus.maintainx.entity.NetworkElementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NetworkElementRepository extends JpaRepository<NetworkElementEntity, Long> {


}
