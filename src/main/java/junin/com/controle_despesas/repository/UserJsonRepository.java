package junin.com.controle_despesas.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import junin.com.controle_despesas.model.User;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserJsonRepository {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File userFile = Paths.get("src/main/resources/data/users.json").toFile();
    private final List<User> cache = new ArrayList<>();
    private long nextId = 1L;

    @PostConstruct
    private void init() {

        try {

            if (userFile.exists()) {

                List<User> users = mapper.readValue(
                        userFile,
                        new TypeReference<List<User>>() {}
                );

                for (User u : users) {

                    u.setId(nextId++);
                    cache.add(u);

                }
            }

            else {

                userFile.getParentFile().mkdirs();
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValue(userFile, new ArrayList<User>());

            }

            cache.forEach(u -> System.out.println(
                    "ID=" + u.getId() +
                            " | email=" + u.getEmail()
            ));

        }

        catch (IOException e) {

            throw new RuntimeException("Erro ao inicializar UsuarioJsonRepository", e);

        }
    }

    public List<User> findAll() {

        return new ArrayList<>(cache);

    }

    public Optional<User> findByEmail(String email) {

        if (email == null) {

            return Optional.empty();

        }

        return cache.stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst();

    }

    public User save(User user) {

        user.setId(nextId++);
        cache.add(user);
        persistUsers();

        return user;

    }

    public void deleteByEmail(String email) {

        cache.removeIf(u -> email.equals(u.getEmail()));
        persistUsers();

    }

    private void persistUsers() {

        try {

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(userFile, cache);

        }

        catch (IOException e) {

            throw new RuntimeException("Falha ao persistir usuarios.json", e);

        }
    }
}
