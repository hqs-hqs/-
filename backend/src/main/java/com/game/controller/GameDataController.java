package com.game.controller;

import com.game.entity.GameData;
import com.game.service.GameDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏数据控制器
 */
@RestController
@RequestMapping("/api/game")
public class GameDataController {
    
    @Autowired
    private GameDataService gameDataService;
    
    /**
     * 获取用户游戏数据
     * GET /api/game/{gameType}/{userId}
     */
    @GetMapping("/{gameType}/{userId}")
    public ResponseEntity<Map<String, Object>> getUserGameData(
            @PathVariable String gameType,
            @PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> data = gameDataService.getUserGameData(userId, gameType);
            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 保存用户游戏数据
     * POST /api/game/{gameType}/{userId}
     */
    @PostMapping("/{gameType}/{userId}")
    public ResponseEntity<Map<String, Object>> saveUserGameData(
            @PathVariable String gameType,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            GameData gameData = gameDataService.saveUserGameData(userId, gameType, data);
            response.put("success", true);
            response.put("message", "保存成功");
            response.put("data", gameData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新用户游戏数据
     * PUT /api/game/{gameType}/{userId}
     */
    @PutMapping("/{gameType}/{userId}")
    public ResponseEntity<Map<String, Object>> updateUserGameData(
            @PathVariable String gameType,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> updates) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            GameData gameData = gameDataService.updateUserGameData(userId, gameType, updates);
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", gameData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

