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
public class MeasureUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "farmer_id")
    private Farmer farmer;

    @Column(unique = true, nullable = false)
    private String name;

    private String abbreviation;
}
