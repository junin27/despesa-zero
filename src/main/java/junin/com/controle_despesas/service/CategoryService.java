package junin.com.controle_despesas.service;

import junin.com.controle_despesas.model.Category;
import junin.com.controle_despesas.repository.CategoryJsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryJsonRepository repo;

    public List<Category> listByUser(Long userId) {

        return repo.findByUserId(userId);

    }

    public Optional<Category> buscarPorId(Long userId, Long id) {

        return listByUser(userId).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();

    }

    public Category create(Category category, Long userId) {

        if (category.getName() == null || category.getName().isBlank()) {

            throw new IllegalArgumentException("Nome da categoria é obrigatório");

        }

        category.setUserId(userId);

        return repo.save(category);

    }

    public Category update(Long userId, Long id, Category dados) {

        Category existente = buscarPorId(userId, id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: " + id));

        if (dados.getName() == null || dados.getName().isBlank()) {

            throw new IllegalArgumentException("Nome da categoria é obrigatório");

        }

        existente.setName(dados.getName());

        return repo.save(existente);

    }

    public void remove(Long userId, Long id) {

        buscarPorId(userId, id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: " + id));
        repo.delete(id);

    }
}