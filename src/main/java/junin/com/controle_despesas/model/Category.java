package junin.com.controle_despesas.model;

public class Category {

    private Long id;
    private String name;
    private Long userId;

    public Category() {

    }

    public Category(Long id, String name, Long userId) {

        this.id = id;
        this.name = name;
        this.userId = userId;

    }

    public Long getId() {

        return id;

    }

    public void setId(Long id) {

        this.id = id;

    }

    public String getName() {

        return name;

    }

    public void setName(String name) {

        this.name = name;

    }

    public Long getUserId() {

        return userId;

    }

    public void setUserId(Long userId) {

        this.userId = userId;

    }
}
