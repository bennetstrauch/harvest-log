package harvestLog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HarvestReport {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;

    @ElementCollection
    @CollectionTable(name = "crop_summary",joinColumns = @JoinColumn(name = "report_id"))
    @MapKeyColumn(name = "crop_name")
    @Column(name = "quantity")
    private Map<String, Double> harvests;
}
