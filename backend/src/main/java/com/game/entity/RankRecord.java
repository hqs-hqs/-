package com.game.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 排行榜记录实体
 */
@Entity
@Table(name = "rank_records")
@Data
public class RankRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId; // 可以为null（五子棋等多玩家游戏不需要userId）
    
    @Column(name = "username", length = 50)
    private String username;
    
    @Column(name = "game_type", nullable = false, length = 50)
    private String gameType; // whackamole, gobang, sudoku, etc.
    
    @Column(name = "mode", length = 50)
    private String mode; // endless, stage, classic, fast, etc.
    
    @Column(name = "score")
    private Integer score;
    
    @Column(name = "time")
    private Integer time; // 用时（秒）
    
    @Column(name = "level")
    private Integer level; // 关卡
    
    @Column(name = "streak")
    private Integer streak; // 连胜数
    
    @Column(columnDefinition = "TEXT")
    private String rankData; // JSON格式的额外数据
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

