package com.snakegame.service;

import com.snakegame.entity.User;
import com.snakegame.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getOrCreateUser(HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        Optional<User> existingUser = userRepository.findByIpAddress(ipAddress);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        String nickname = generateNickname();
        User newUser = new User(ipAddress, nickname);
        return userRepository.save(newUser);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateUserScore(Long userId, Integer gameScore, Boolean isWin) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        user.setScore(user.getScore() + gameScore);
        user.setTotalGames(user.getTotalGames() + 1);

        if (isWin) {
            user.setWins(user.getWins() + 1);
        }

        if (gameScore > user.getMaxScore()) {
            user.setMaxScore(gameScore);
        }

        return userRepository.save(user);
    }

    public User updateNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }
        user.setNickname(nickname);
        return userRepository.save(user);
    }

    public List<User> getLeaderboardByScore() {
        return userRepository.findTop100ByOrderByScoreDesc();
    }

    public List<User> getLeaderboardByMaxScore() {
        return userRepository.findTop100ByOrderByMaxScoreDesc();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String generateNickname() {
        String[] adjectives = {"赚钱的", "牛市", "涨停", "翻倍", "分红", "盈利", "获利", "丰收", "增值", "盈利"};
        String[] nouns = {"韭菜", "大户", "庄家", "游资", "散户", "机构", "基金", "投顾", "交易员", "操盘手"};
        int adj = (int) (Math.random() * adjectives.length);
        int noun = (int) (Math.random() * nouns.length);
        return adjectives[adj] + nouns[noun];
    }

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByIdAsc();
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
