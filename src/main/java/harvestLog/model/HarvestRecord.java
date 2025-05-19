package harvestLog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor


public class HarvestRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    LocalDate date;
    @OneToOne
    Crop crop;
    @OneToMany
    List<Field> fields;

    double harvestedQuantity;
    @ManyToOne
    Farmer farmer;

}
