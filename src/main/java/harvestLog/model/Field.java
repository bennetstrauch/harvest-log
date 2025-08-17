package harvestLog.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Field implements SoftActivatable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
     Long id;

    String name;

// #leave it lazy?
    @ManyToOne(fetch = FetchType.LAZY)
    Farmer farmer;

    @Column(nullable = false)
    private boolean active = true;

}



