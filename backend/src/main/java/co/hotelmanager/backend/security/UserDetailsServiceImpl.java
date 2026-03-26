package co.hotelmanager.backend.security;

import co.hotelmanager.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    // Spring Security llama este método automáticamente
    // durante el login para cargar el usuario desde la BD.
    // El "username" en nuestro caso es el email.
    @Override
    public UserDetails loadUserByUsername(String email)
        throws UsernameNotFoundException {

        return usuarioRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "Usuario no encontrado con email: " + email
                )
            );
    }
}