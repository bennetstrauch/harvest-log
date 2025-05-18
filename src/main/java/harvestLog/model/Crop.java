package harvestLog.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Crop {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    String name;
    MeasureUnit measureUnit;
}
