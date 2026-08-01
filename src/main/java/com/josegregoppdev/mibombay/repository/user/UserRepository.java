package com.josegregoppdev.mibombay.repository.user;

import com.josegregoppdev.mibombay.model.user.Role;
import com.josegregoppdev.mibombay.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByTenantIdAndEmail(String tenantId, String email);

    List<User> findByTenantId(String tenantId);

    List<User> findByTenantIdAndRole(String tenantId, Role role);

    boolean existsByEmail(String email);

    boolean existsByTenantIdAndEmail(String tenantId, String email);
}
