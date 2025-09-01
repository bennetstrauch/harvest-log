package harvestLog.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"farmer_id", "name"})
        }
)
public class MeasureUnit implements SoftActivatable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "farmer_id")
    private Farmer farmer;

    @Column(nullable = false)
    private String name;

    private String abbreviation;

    @Column(nullable = false)
    private boolean active = true;

    // ✅ Convenience constructor for creation (avoids passing active flag)
    public MeasureUnit(Farmer farmer, String name, String abbreviation) {
        this.farmer = farmer;
        this.name = name;
        this.abbreviation = abbreviation;
        this.active = true;
    }
}
