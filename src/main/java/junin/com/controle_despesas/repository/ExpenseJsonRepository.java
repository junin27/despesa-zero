package junin.com.controle_despesas.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import junin.com.controle_despesas.model.Expense;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ExpenseJsonRepository {

    private final File file = new File("src/main/resources/data/expense.json");
    private final ObjectMapper mapper;

    public ExpenseJsonRepository() {

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

    }

    public List<Expense> findAll() {

        try {

            if (!file.exists()) return new ArrayList<>();

            return mapper.readValue(file, new TypeReference<>() {});

        }

        catch (Exception e) {

            System.out.println("Erro ao ler despesas: " + e.getMessage());

            return new ArrayList<>();

        }
    }

    public List<Expense> findByUserId(Long userId) {

        return findAll().stream().filter(e -> e.getUserId().equals(userId)).toList();

    }

    public Expense findById(Long id) {

        return findAll().stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);

    }

    public void save(Expense expense) {

        List<Expense> all = findAll();

        if (expense.getId() == null) {

            long nextId = all.stream().mapToLong(e -> e.getId() != null ? e.getId() : 0).max().orElse(0) + 1;
            expense.setId(nextId);

        }

        else {

            all.removeIf(e -> e.getId().equals(expense.getId()));

        }

        all.add(expense);

        try {

            mapper.writeValue(file, all);

        }

        catch (Exception e) {

            System.out.println("Erro ao salvar despesas: " + e.getMessage());

        }
    }

    public void delete(Long id) {

        List<Expense> all = findAll();
        all.removeIf(e -> e.getId().equals(id));

        try {

            mapper.writeValue(file, all);

        }

        catch (Exception e) {

            System.out.println("Erro ao deletar despesas: " + e.getMessage());

        }
    }
}
