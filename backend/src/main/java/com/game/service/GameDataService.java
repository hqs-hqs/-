package com.game.service;

import com.game.entity.GameData;
import com.game.repository.GameDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 游戏数据服务
 */
@Service
public class GameDataService {
    
    @Autowired
    private GameDataRepository gameDataRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 获取用户游戏数据
     */
    public Map<String, Object> getUserGameData(Long userId, String gameType) {
        Optional<GameData> gameDataOpt = gameDataRepository.findByUserIdAndGameType(userId, gameType);
        
        if (gameDataOpt.isPresent()) {
            try {
                String dataJson = gameDataOpt.get().getData();
                if (dataJson != null && !dataJson.isEmpty()) {
                    return objectMapper.readValue(dataJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 返回默认数据
        return new HashMap<>();
    }
    
    /**
     * 保存用户游戏数据
     */
    public GameData saveUserGameData(Long userId, String gameType, Map<String, Object> data) {
        try {
            String dataJson = objectMapper.writeValueAsString(data);
            
            Optional<GameData> existingOpt = gameDataRepository.findByUserIdAndGameType(userId, gameType);
            GameData gameData;
            
            if (existingOpt.isPresent()) {
                // 更新现有数据
                gameData = existingOpt.get();
                gameData.setData(dataJson);
            } else {
                // 创建新数据
                gameData = new GameData();
                gameData.setUserId(userId);
                gameData.setGameType(gameType);
                gameData.setData(dataJson);
            }
            
            return gameDataRepository.save(gameData);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("保存游戏数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户游戏数据（部分更新）
     */
    public GameData updateUserGameData(Long userId, String gameType, Map<String, Object> updates) {
        Optional<GameData> existingOpt = gameDataRepository.findByUserIdAndGameType(userId, gameType);
        
        Map<String, Object> currentData;
        if (existingOpt.isPresent()) {
            try {
                String dataJson = existingOpt.get().getData();
                if (dataJson != null && !dataJson.isEmpty()) {
                    currentData = objectMapper.readValue(dataJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                } else {
                    currentData = new HashMap<>();
                }
            } catch (Exception e) {
                currentData = new HashMap<>();
            }
        } else {
            currentData = new HashMap<>();
        }
        
        // 合并更新数据
        currentData.putAll(updates);
        
        return saveUserGameData(userId, gameType, currentData);
    }
}

