package com.virtual.aulacristo.config;

import com.virtual.aulacristo.entity.Usuario;
import com.virtual.aulacristo.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;

    public SecurityConfig(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Bean
    public ActiveDirectoryLdapAuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
        ActiveDirectoryLdapAuthenticationProvider provider =
                new ActiveDirectoryLdapAuthenticationProvider("cristorey.local", "ldap://192.168.1.15:389");

        // CORRECCIÓN: Usar {1} para que busque solo el sAMAccountName ("mdepaz")
        provider.setSearchFilter("(&(objectClass=user)(sAMAccountName={1}))");

        // Muestra en consola el motivo exacto del fallo (cuenta expirada, bloqueada, etc.)
        provider.setConvertSubErrorCodesToExceptions(true);

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**",
                                "/js/**",
                                "/fonts/**",
                                "/images/**",
                                "/favicon.ico",
                                "/login").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler()) // Puente para poblar los datos en la sesión
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            HttpSession session = request.getSession();

            // 1. Extraemos el username limpio enviado por Active Directory (ej. "fmercado")
            String username = "";
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.ldap.userdetails.LdapUserDetails) {
                username = ((org.springframework.security.ldap.userdetails.LdapUserDetails) principal).getUsername();
            }
            if (username == null || username.isEmpty()) {
                username = authentication.getName();
            }

            // 2. Buscamos al usuario en la BD
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);

            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                session.setAttribute("usuarioLogueado", usuario);

                // 3. Obtenemos el rol REAL de la BD leyendo directamente el objeto/ID o el granted authority
                String rolReal = "ESTUDIANTE";

                // Si Spring Security ya mapeó las autoridades desde Active Directory o BD:
                if (!authentication.getAuthorities().isEmpty()) {
                    // Obtiene la autoridad/rol otorgado por Spring Security (ej. "Tesoreria" o "TESORERIA")
                    String authority = authentication.getAuthorities().iterator().next().getAuthority();
                    rolReal = authority.toUpperCase().replace("ROLE_", "");
                }

                // Normalización de nombres de roles según tu tabla de BD
                if (rolReal.contains("TESORERIA")) {
                    rolReal = "TESORERIA";
                } else if (rolReal.contains("SECRETARIA")) {
                    rolReal = "SECRETARIA";
                } else if (rolReal.contains("ADMINISTRADOR")) {
                    rolReal = "ADMINISTRADOR";
                }

                session.setAttribute("rolUsuario", rolReal);
            } else {
                session.setAttribute("rolUsuario", "TESORERIA");
            }

            response.sendRedirect("/");
        };
    }
}
