package harvestLog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    LocalDateTime createdAt;
    @ManyToOne
    Crop crop;

    @ManyToMany
    List<Field> fields;

    double harvestedQuantity;
    @ManyToOne(fetch = FetchType.LAZY)
    Farmer farmer;

    @Column(nullable = false, columnDefinition = "boolean default false")
    boolean archived = false;

    @Column(nullable = true)
    String archivedCropName;

    @Column(nullable = true)
    String archivedFieldNames;

    @Column(nullable = true)
    String archivedMeasureUnitName;

    @Column(nullable = true)
    String archivedCategoryName;
}
