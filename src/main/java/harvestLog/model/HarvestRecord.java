package harvestLog.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
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
