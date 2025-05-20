package junin.com.controle_despesas.repository;

import java.util.List;
import java.util.Optional;

public interface JsonRepository<T> {

    List<T> findAll();

    Optional<T> findById(Long id);

    T save(T entity);

    void delete(Long id);

}
