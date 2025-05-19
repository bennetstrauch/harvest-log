package harvestLog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Farmer {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String name;
    @Column(unique = true)
    private String email;

    private String password;


    @OneToMany(mappedBy = "farmer", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<HarvestRecord> harvestRecords = new ArrayList<>();

    //If you ever need to fetch a crop and know which farmer it belongs to, make it bidirectional.
    @OneToMany(mappedBy = "farmer", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Crop> crops = new ArrayList<>();

    @OneToMany(mappedBy = "farmer",cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Field> fields = new ArrayList<>();

    public void addHarvestRecord(HarvestRecord record) {
        harvestRecords.add(record);
        record.setFarmer(this);
    }

    public void addCrop(Crop crop) {
        crops.add(crop);
        crop.setFarmer(this);
    }

    public void addField(Field field) {
        fields.add(field);
        field.setFarmer(this);
    }

}
