package harvestLog.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Farmer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    String name;
//    unique!#
    String email;
    String password;

    @OneToMany( mappedBy = "farmer")
    List<HarvestRecord> harvestRecords;
    @OneToMany
    List<Crop> crops;
    @OneToMany
    List<Field> fields;

}
