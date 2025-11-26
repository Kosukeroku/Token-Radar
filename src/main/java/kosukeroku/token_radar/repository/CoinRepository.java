package kosukeroku.token_radar.repository;


import jakarta.persistence.Cacheable;
import kosukeroku.token_radar.model.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoinRepository extends JpaRepository<Coin, String> {
    @Query("SELECT c.id FROM Coin c WHERE c.active = true")
    List<String> findAllActiveCoinIds();

    @Modifying
    @Query("DELETE FROM Coin c WHERE c.active = false")
    void deleteByActiveFalse();
}
