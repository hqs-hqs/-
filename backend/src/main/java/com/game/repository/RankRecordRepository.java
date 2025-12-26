package com.game.repository;

import com.game.entity.RankRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankRecordRepository extends JpaRepository<RankRecord, Long> {
    
    /**
     * 根据游戏类型和模式查询排行榜（按分数降序，取前N条）
     */
    @Query("SELECT r FROM RankRecord r WHERE r.gameType = :gameType AND r.mode = :mode ORDER BY r.score DESC, r.time ASC")
    List<RankRecord> findTopByGameTypeAndModeOrderByScoreDesc(@Param("gameType") String gameType, @Param("mode") String mode);
    
    /**
     * 根据游戏类型和模式查询排行榜（按时间升序，取前N条）- 用于数独等按时间排名的游戏
     */
    @Query("SELECT r FROM RankRecord r WHERE r.gameType = :gameType AND r.mode = :mode ORDER BY r.time ASC")
    List<RankRecord> findTopByGameTypeAndModeOrderByTimeAsc(@Param("gameType") String gameType, @Param("mode") String mode);
    
    /**
     * 根据游戏类型和模式查询排行榜（按连胜数降序）- 用于五子棋
     */
    @Query("SELECT r FROM RankRecord r WHERE r.gameType = :gameType AND r.mode = :mode ORDER BY r.streak DESC")
    List<RankRecord> findTopByGameTypeAndModeOrderByStreakDesc(@Param("gameType") String gameType, @Param("mode") String mode);
    
    /**
     * 根据游戏类型、模式和关卡查询排行榜
     */
    @Query("SELECT r FROM RankRecord r WHERE r.gameType = :gameType AND r.mode = :mode AND r.level = :level ORDER BY r.time ASC")
    List<RankRecord> findTopByGameTypeAndModeAndLevelOrderByTimeAsc(
        @Param("gameType") String gameType, 
        @Param("mode") String mode, 
        @Param("level") Integer level
    );
    
    /**
     * 根据用户ID、游戏类型和模式查询记录
     */
    RankRecord findByUserIdAndGameTypeAndMode(Long userId, String gameType, String mode);
    
    /**
     * 根据用户ID、游戏类型、模式和关卡查询记录（用于打地鼠闯关模式）
     */
    RankRecord findByUserIdAndGameTypeAndModeAndLevel(Long userId, String gameType, String mode, Integer level);
    
    /**
     * 根据玩家名、游戏类型和模式查询记录（用于五子棋等多玩家游戏）
     */
    RankRecord findByUsernameAndGameTypeAndMode(String username, String gameType, String mode);
}

