package kosukeroku.token_radar.repository;


import kosukeroku.token_radar.model.Coin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoinRepository extends JpaRepository<Coin, String> {
    @Query("SELECT c.id FROM Coin c WHERE c.active = true")
    List<String> findAllActiveCoinIds();

    Page<Coin> findByActiveTrue(Pageable pageable);

    @Modifying
    @Query("DELETE FROM Coin c WHERE c.active = false")
    void deleteByActiveFalse();
}
