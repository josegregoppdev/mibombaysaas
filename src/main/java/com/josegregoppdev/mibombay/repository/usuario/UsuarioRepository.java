package com.josegregoppdev.mibombay.repository.usuario;

import com.josegregoppdev.mibombay.model.usuario.Rol;
import com.josegregoppdev.mibombay.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByTenantIdAndEmail(String tenantId, String email);

    List<Usuario> findByTenantId(String tenantId);

    List<Usuario> findByTenantIdAndRol(String tenantId, Rol rol);

    boolean existsByEmail(String email);

    boolean existsByTenantIdAndEmail(String tenantId, String email);
}
