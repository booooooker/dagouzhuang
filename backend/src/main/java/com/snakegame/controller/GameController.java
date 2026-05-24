package com.snakegame.controller;

import com.snakegame.entity.User;
import com.snakegame.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GameController {

    @Autowired
    private UserService userService;

    @GetMapping("/user")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        User user = userService.getOrCreateUser(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/game/end")
    public ResponseEntity<Map<String, Object>> endGame(
            @RequestParam Long userId,
            @RequestParam Integer score,
            @RequestParam Boolean isWin,
            HttpServletRequest request) {
        User user = userService.updateUserScore(userId, score, isWin);
        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            response.put("success", true);
            response.put("totalScore", user.getScore());
            response.put("totalGames", user.getTotalGames());
            response.put("wins", user.getWins());
            response.put("maxScore", user.getMaxScore());
        } else {
            response.put("success", false);
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/nickname")
    public ResponseEntity<User> updateNickname(
            @RequestParam Long userId,
            @RequestParam String nickname) {
        User user = userService.updateNickname(userId, nickname);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/leaderboard/score")
    public ResponseEntity<List<User>> getLeaderboardByScore() {
        return ResponseEntity.ok(userService.getLeaderboardByScore());
    }

    @GetMapping("/leaderboard/maxscore")
    public ResponseEntity<List<User>> getLeaderboardByMaxScore() {
        return ResponseEntity.ok(userService.getLeaderboardByMaxScore());
    }

    @PostMapping("/admin/login")
    public ResponseEntity<Map<String, Object>> adminLogin(@RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        if ("buge2026".equals(password)) {
            response.put("success", true);
            response.put("message", "登录成功");
        } else {
            response.put("success", false);
            response.put("message", "密码错误");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/admin/user/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.deleteUser(userId);
            response.put("success", true);
            response.put("message", "删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败");
        }
        return ResponseEntity.ok(response);
    }
}
