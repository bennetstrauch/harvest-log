package harvestLog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"farmer_id", "name"}))
public class Category implements SoftActivatable{

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "farmer_id")
    private Farmer farmer;

    @Column(nullable = false)
    private boolean active = true;

    public Category(String name, Farmer farmer) {
        this.name = name.toUpperCase();
        this.farmer = farmer;
        this.active = true;
    }

}