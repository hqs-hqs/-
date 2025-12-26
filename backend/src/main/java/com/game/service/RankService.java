package com.game.service;

import com.game.entity.RankRecord;
import com.game.repository.RankRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 排行榜服务
 */
@Service
public class RankService {
    
    @Autowired
    private RankRecordRepository rankRecordRepository;
    
    /**
     * 获取排行榜（按分数降序）
     */
    public List<RankRecord> getRankListByScore(String gameType, String mode, int limit) {
        List<RankRecord> records = rankRecordRepository.findTopByGameTypeAndModeOrderByScoreDesc(gameType, mode);
        return records.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取排行榜（按时间升序）- 用于数独等
     */
    public List<RankRecord> getRankListByTime(String gameType, String mode, int limit) {
        List<RankRecord> records = rankRecordRepository.findTopByGameTypeAndModeOrderByTimeAsc(gameType, mode);
        return records.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取排行榜（按连胜数降序）- 用于五子棋
     */
    public List<RankRecord> getRankListByStreak(String gameType, String mode, int limit) {
        List<RankRecord> records = rankRecordRepository.findTopByGameTypeAndModeOrderByStreakDesc(gameType, mode);
        return records.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 获取排行榜（按关卡和时间）- 用于打地鼠闯关模式
     */
    public List<RankRecord> getRankListByLevel(String gameType, String mode, Integer level, int limit) {
        List<RankRecord> records = rankRecordRepository.findTopByGameTypeAndModeAndLevelOrderByTimeAsc(gameType, mode, level);
        return records.stream().limit(limit).collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 提交排行榜记录
     * 
     * 五子棋：按玩家名（username）区分，userId可以为null，每个玩家一条记录
     * 打地鼠无尽模式：按userId区分，每个用户一条记录
     * 打地鼠闯关模式：按userId+level区分，每个用户每个关卡一条记录
     * 数独：按userId区分，但可以有多条记录（不同日期），所以总是创建新记录
     */
    @Transactional
    public RankRecord submitRankRecord(Long userId, String username, String gameType, String mode, 
                                       Integer score, Integer time, Integer level, Integer streak, String rankData) {
        RankRecord existingRecord = null;
        
        if (gameType.equals("gobang")) {
            // 五子棋：按玩家名查找（因为可以添加任意玩家，不一定是登录用户）
            existingRecord = rankRecordRepository.findByUsernameAndGameTypeAndMode(username, gameType, mode);
        } else if (gameType.equals("whackamole") && mode != null && mode.equals("stage") && level != null) {
            // 打地鼠闯关模式：按userId和level查找
            existingRecord = rankRecordRepository.findByUserIdAndGameTypeAndModeAndLevel(userId, gameType, mode, level);
        } else if (gameType.equals("whackamole") && mode != null && mode.equals("endless")) {
            // 打地鼠无尽模式：总是创建新记录（支持多条记录，每次游戏都记录）
            // 这样排行榜可以显示同一个用户的多次游戏记录，匹配前端localStorage的逻辑
            existingRecord = null;
        } else if (gameType.equals("sudoku")) {
            // 数独：可以有多条记录（不同日期），所以不查找，直接创建新记录
            existingRecord = null;
        } else {
            // 其他游戏：按userId查找
            if (userId != null) {
                existingRecord = rankRecordRepository.findByUserIdAndGameTypeAndMode(userId, gameType, mode);
            }
        }
        
        if (existingRecord != null) {
            // 更新现有记录（根据游戏类型决定更新策略）
            boolean shouldUpdate = false;
            
            if (gameType.equals("gobang")) {
                // 五子棋：更新连胜数（取较大值）
                if (streak != null && (existingRecord.getStreak() == null || streak > existingRecord.getStreak())) {
                    existingRecord.setStreak(streak);
                    shouldUpdate = true;
                }
            } else if (gameType.equals("whackamole") && mode != null && mode.equals("stage")) {
                // 打地鼠闯关模式：更新时间（取较小值）
                if (time != null && (existingRecord.getTime() == null || time < existingRecord.getTime())) {
                    existingRecord.setTime(time);
                    shouldUpdate = true;
                }
            }
            
            if (shouldUpdate) {
                if (level != null) existingRecord.setLevel(level);
                if (rankData != null) existingRecord.setRankData(rankData);
                return rankRecordRepository.save(existingRecord);
            }
            
            return existingRecord;
        } else {
            // 创建新记录
            RankRecord newRecord = new RankRecord();
            // 五子棋：userId可以为null
            if (!gameType.equals("gobang")) {
                newRecord.setUserId(userId);
            }
            newRecord.setUsername(username);
            newRecord.setGameType(gameType);
            newRecord.setMode(mode);
            newRecord.setScore(score);
            newRecord.setTime(time);
            newRecord.setLevel(level);
            newRecord.setStreak(streak);
            newRecord.setRankData(rankData);
            
            return rankRecordRepository.save(newRecord);
        }
    }
    
    /**
     * 获取所有排行榜（通用方法，根据游戏类型自动选择排序方式）
     */
    public List<RankRecord> getRankList(String gameType, String mode, int limit) {
        if (gameType.equals("gobang")) {
            return getRankListByStreak(gameType, mode, limit);
        } else if (gameType.equals("sudoku")) {
            return getRankListByTime(gameType, mode, limit);
        } else {
            return getRankListByScore(gameType, mode, limit);
        }
    }
}

