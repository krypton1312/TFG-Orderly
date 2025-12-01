package com.yebur.backendorderly.supplements;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplementRepository extends JpaRepository<Supplement, Long> {

    List<Supplement> findAll();

    Optional<Supplement> findSupplementById(Long id);

    List<Supplement> findSupplementByCategoriesId(Long categoryId);

}
