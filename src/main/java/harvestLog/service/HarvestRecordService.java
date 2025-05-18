//package harvestLog.service;
//
//import harvestLog.model.HarvestRecord;
//import harvestLog.repository.HarvestRecordRepo;
//import jakarta.persistence.criteria.CriteriaBuilder;
//import jakarta.persistence.criteria.CriteriaQuery;
//import jakarta.persistence.criteria.Root;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Predicate;
//
//@Service
//class HarvestRecordService {
//    private final HarvestRecordRepo recordRepo;
//
//    public HarvestRecordService(HarvestRecordRepo recordRepo) {
//        this.recordRepo = recordRepo;
//    }
//
//    public List<HarvestRecord> getForFarmer(Long farmerId) {
//        return recordRepo.findByFarmerId(farmerId);
//    }
//
//    public List<HarvestRecord> getFilteredRecords(Long farmerId, List<Long> fieldIds, List<Long> cropIds,
//                                                  LocalDate startDate, LocalDate endDate) {
//        return recordRepo.findAll((Root<HarvestRecord> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            if (farmerId != null) {
//                predicates.add(cb.equal(root.get("farmer").get("id"), farmerId));
//            }
//            if (fieldIds != null && !fieldIds.isEmpty()) {
//                predicates.add(root.get("fields").get("id").in(fieldIds));
//            }
//            if (cropIds != null && !cropIds.isEmpty()) {
//                predicates.add(root.get("crop").get("id").in(cropIds));
//            }
//            if (startDate != null) {
//                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
//            }
//            if (endDate != null) {
//                predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
//            }
//
//            return cb.and(predicates.toArray(new Predicate[0]));
//        });
//    }
//
//    public HarvestRecord create(HarvestRecord record) {
//        return recordRepo.save(record);
//    }
//
//    public Optional<HarvestRecord> update(Long id, HarvestRecord updatedRecord) {
//        return recordRepo.findById(id)
//                .map(existing -> {
//                    existing.setDate(updatedRecord.getDate());
//                    existing.setCrop(updatedRecord.getCrop());
//                    existing.setFields(updatedRecord.getFields());
//                    existing.setHarvestedQuantity(updatedRecord.getHarvestedQuantity());
//                    existing.setFarmer(updatedRecord.getFarmer());
//                    return recordRepo.save(existing);
//                });
//    }
//
//    public boolean delete(Long id) {
//        if (recordRepo.existsById(id)) {
//            recordRepo.deleteById(id);
//            return true;
//        }
//        return false;
//    }
//}
