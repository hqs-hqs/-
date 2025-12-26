package com.game.repository;

import com.game.entity.GameData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameDataRepository extends JpaRepository<GameData, Long> {
    /**
     * 根据用户ID和游戏类型查询游戏数据
     */
    Optional<GameData> findByUserIdAndGameType(Long userId, String gameType);
}

