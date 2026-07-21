package com.josegregoppdev.mibombay.repository.empresa;

import com.josegregoppdev.mibombay.model.empresa.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByTenantId(String tenantId);

    Optional<Empresa> findBySubdominio(String subdominio);

    boolean existsBySubdominio(String subdominio);

    boolean existsByTenantId(String tenantId);
}
