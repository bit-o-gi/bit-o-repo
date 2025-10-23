package org.bito.concert.repository;

import org.bito.concert.model.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, Long> {

    @Query("SELECT c FROM Concert c WHERE c.price <= ?1 ORDER BY c.price ASC")
    List<Concert> findCheapConcerts(Integer maxPrice);

    @Query("SELECT c FROM Concert c ORDER BY c.date ASC")
    List<Concert> findAllOrderByDate();
}
