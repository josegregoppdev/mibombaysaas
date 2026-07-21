package com.josegregoppdev.mibombay.service.usuario;

import com.josegregoppdev.mibombay.common.tenant.TenantContext;
import com.josegregoppdev.mibombay.model.usuario.Usuario;
import com.josegregoppdev.mibombay.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        TenantContext.set(usuario.getTenantId());

        return new User(
                usuario.getEmail(),
                usuario.getPasswordHash(),
                usuario.getActivo(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
        );
    }
}
