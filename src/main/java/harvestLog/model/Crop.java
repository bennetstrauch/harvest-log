package harvestLog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Crop {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String name;
    @Enumerated(EnumType.STRING)
    private MeasureUnit measureUnit;

    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private Farmer farmer;
}
