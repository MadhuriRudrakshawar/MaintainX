/**
 * Repository interface for network element.
 * Handles database access for network element.
 */

package com.tus.maintainx.repository;

import com.tus.maintainx.entity.NetworkElementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NetworkElementRepository extends JpaRepository<NetworkElementEntity, Long> {


}
