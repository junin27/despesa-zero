package junin.com.controle_despesas.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense {

    private Long id;
    private Long userId;
    private Long categoryId;
    private BigDecimal value;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    public Long getId() {

        return id;

    }

    public void setId(Long id) {

        this.id = id;

    }

    public Long getUserId() {

        return userId;

    }

    public void setUserId(Long userId) {

        this.userId = userId;

    }

    public Long getCategoryId() {

        return categoryId;

    }

    public void setCategoryId(Long categoryId) {

        this.categoryId = categoryId;

    }

    public BigDecimal getValue() {

        return value;

    }

    public void setValue(BigDecimal value) {

        this.value = value;

    }

    public LocalDate getDate() {

        return date;

    }

    public void setDate(LocalDate date) {

        this.date = date;

    }
}
