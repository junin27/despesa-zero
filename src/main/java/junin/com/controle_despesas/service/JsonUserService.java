package junin.com.controle_despesas.service;

import junin.com.controle_despesas.model.User;
import junin.com.controle_despesas.repository.UserJsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JsonUserService implements UserDetailsService {

    @Autowired
    private UserJsonRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        System.out.println("Tentando autenticar: " + email);

        User u = repo.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("Usuário não encontrado!");
                    return new UsernameNotFoundException("Usuário não encontrado");
                });

        System.out.println("Usuário encontrado: " + u.getEmail());
        System.out.println("Senha criptografada salva: " + u.getPassword());

        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getEmail())
                .password(u.getPassword())
                .roles("USER")
                .build();
    }

    /**
     * Registra um novo usuário.
     * @param novo objeto User com email, senha e confirmPassword preenchidos.
     */

    public void register(User novo) {

        if (!novo.getPassword().equals(novo.getConfirmPassword())) {

            throw new IllegalArgumentException("As senhas inseridas não conferem");

        }

        if (repo.findByEmail(novo.getEmail()).isPresent()) {

            throw new IllegalArgumentException("Este e-mail já está em uso");

        }

        String hashed = passwordEncoder.encode(novo.getPassword());
        novo.setPassword(hashed);
        repo.save(novo);

    }
}
