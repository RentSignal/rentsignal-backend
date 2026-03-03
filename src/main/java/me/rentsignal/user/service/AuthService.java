package me.rentsignal.user.service;

import lombok.RequiredArgsConstructor;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.user.entity.User;
import me.rentsignal.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    /** provider로부터 nickname이나 name을 제공받지 않은 경우 임시 닉네임 생성 */
    public String generateTempName() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional
    public void registerPhone(Long userId, String phone) {
        User user = getCurrentUser(userId);
        // phone 등록 + role 변경
        user.registerPhone(phone);
    }

    /** 현재 로그인한 User 객체 가져오기 */
    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }

}
