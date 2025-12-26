package com.game.controller;

import com.game.entity.RankRecord;
import com.game.service.RankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排行榜控制器
 */
@RestController
@RequestMapping("/api/rank")
public class RankController {
    
    @Autowired
    private RankService rankService;
    
    /**
     * 获取排行榜
     * GET /api/rank/{gameType}?mode=xxx&limit=10
     */
    @GetMapping("/{gameType}")
    public ResponseEntity<Map<String, Object>> getRankList(
            @PathVariable String gameType,
            @RequestParam(required = false) String mode,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<RankRecord> rankList = rankService.getRankList(gameType, mode, limit);
            response.put("success", true);
            response.put("data", rankList);
            response.put("count", rankList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 提交排行榜记录
     * POST /api/rank/{gameType}
     */
    @PostMapping("/{gameType}")
    public ResponseEntity<Map<String, Object>> submitRank(
            @PathVariable String gameType,
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // userId可以为null（五子棋等多玩家游戏不需要userId）
            Long userId = null;
            if (request.get("userId") != null) {
                userId = Long.valueOf(request.get("userId").toString());
            }
            String username = request.get("username") != null ? request.get("username").toString() : null;
            String mode = request.get("mode") != null ? request.get("mode").toString() : null;
            Integer score = request.get("score") != null ? Integer.valueOf(request.get("score").toString()) : null;
            Integer time = request.get("time") != null ? Integer.valueOf(request.get("time").toString()) : null;
            Integer level = request.get("level") != null ? Integer.valueOf(request.get("level").toString()) : null;
            Integer streak = request.get("streak") != null ? Integer.valueOf(request.get("streak").toString()) : null;
            String rankData = request.get("rankData") != null ? request.get("rankData").toString() : null;
            
            RankRecord record = rankService.submitRankRecord(userId, username, gameType, mode, 
                    score, time, level, streak, rankData);
            
            response.put("success", true);
            response.put("message", "排行榜记录已保存");
            response.put("data", record);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("提交排行榜失败: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取特定关卡的排行榜（用于打地鼠等）
     * GET /api/rank/{gameType}/level/{level}?mode=xxx
     */
    @GetMapping("/{gameType}/level/{level}")
    public ResponseEntity<Map<String, Object>> getRankListByLevel(
            @PathVariable String gameType,
            @PathVariable Integer level,
            @RequestParam(required = false) String mode,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<RankRecord> rankList = rankService.getRankListByLevel(gameType, mode, level, limit);
            response.put("success", true);
            response.put("data", rankList);
            response.put("count", rankList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

