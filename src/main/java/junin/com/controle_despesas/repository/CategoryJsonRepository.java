package junin.com.controle_despesas.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import junin.com.controle_despesas.model.Category;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class CategoryJsonRepository {
    private final ObjectMapper mapper = new ObjectMapper();
    private final File file = Paths.get("src/main/resources/data/categories.json").toFile();
    private List<Category> cache = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    @PostConstruct
    private void init() {

        try {
            File parent = file.getParentFile();
            if (!parent.exists()) parent.mkdirs();

            if (!file.exists()) {

                mapper.writerWithDefaultPrettyPrinter().writeValue(file, cache);

            }

            else {

                cache = mapper.readValue(file, new TypeReference<List<Category>>() {});

            }

            long maxId = cache.stream()
                    .map(Category::getId)
                    .filter(Objects::nonNull)
                    .max(Long::compareTo)
                    .orElse(0L);
            counter.set(maxId);
        }

        catch (IOException e) {

            throw new RuntimeException("Falha ao inicializar categorias", e);

        }
    }

    public Category save(Category category) {

        if (category.getId() == null) {

            category.setId(counter.incrementAndGet());

        }

        findById(category.getId()).ifPresent(cache::remove);
        cache.add(category);
        persist();

        return category;

    }

    public Optional<Category> findById(Long id) {

        return cache.stream()
                .filter(c -> Objects.equals(c.getId(), id))
                .findFirst();

    }

    public List<Category> findByUserId(Long userId) {

        List<Category> result = new ArrayList<>();

        for (Category c : cache) {

            if (Objects.equals(c.getUserId(), userId)) {

                result.add(c);

            }
        }

        return result;

    }

    public void delete(Long id) {

        findById(id).ifPresent(cache::remove);
        persist();

    }

    private void persist() {

        try {

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, cache);

        }

        catch (IOException e) {

            throw new RuntimeException("Falha ao salvar categorias", e);

        }
    }
}
