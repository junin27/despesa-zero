package junin.com.controle_despesas.service;

import junin.com.controle_despesas.model.Expense;
import junin.com.controle_despesas.repository.ExpenseJsonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseJsonRepository repo;

    public List<Expense> listByUser(Long userId) {

        return repo.findByUserId(userId);

    }

    public Expense searchById(Long id) {

        return repo.findById(id);

    }

    public void save(Expense expense) {

        repo.save(expense);

    }

    public void delete(Long id) {

        repo.delete(id);

    }

    public List<Expense> listAll() {

        return repo.findAll();

    }
}
