package me.rentsignal.global.security;

import lombok.RequiredArgsConstructor;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import me.rentsignal.user.entity.Provider;
import me.rentsignal.user.entity.Role;
import me.rentsignal.user.entity.User;
import me.rentsignal.user.entity.UserSocialAccount;
import me.rentsignal.user.repository.UserRepository;
import me.rentsignal.user.repository.UserSocialAccountRepository;
import me.rentsignal.user.service.AuthService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Provider로부터 받은 정보를 User로 매핑
 * -> CustomPrincipal로 반환
 * */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserSocialAccountRepository userSocialAccountRepository;
    private final AuthService authService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. provider에서 사용자 정보 (attributes) 조회
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. 어떤 provider인지 확인 (application.yml의 registrationId)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. provider별 OAuth2UserInfo 객체 생성
        OAuth2UserInfo userInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        Provider provider = Provider.valueOf(userInfo.getProvider().toUpperCase());
        String providerId = userInfo.getProviderId();

        // 4. 기존 소셜 계정 존재 여부 확인
        // - 있을 경우 기존 user 가져오기, 없을 경우 생성
        User user = userSocialAccountRepository.findByProviderAndProviderUserId(provider, providerId)
                .map(UserSocialAccount::getUser)
                .orElseGet(() -> createUserWithSocialAccount(provider, providerId, userInfo));

        return CustomPrincipal
                .fromOAuth(user.getId(), user.getRole(), oAuth2User.getAttributes());
    }

    /** registrationId에 따라 적절한 OAuth2UserInfo 구현체 생성 */
    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return new NaverOAuth2UserInfo(attributes);
        }

        throw new BaseException(ErrorCode.INVALID_SOCIAL_PROVIDER, "지원하지 않는 소셜 로그인입니다. - " + registrationId);
    }

    /** 신규 user 생성 + 소셜 계정 연결 */
    @Transactional
    public User createUserWithSocialAccount(Provider provider,
                                             String providerUserId,
                                             OAuth2UserInfo userInfo) {
        // name이 null이면 랜던 생성
        String name = userInfo.getName();
        if (name == null || name.isEmpty())
            name = authService.generateTempName();

        User user = userRepository.save(
                User.builder().name(name)
                        .role(Role.ROLE_GUEST).build()
        );

        UserSocialAccount socialAccount = UserSocialAccount.builder()
                .provider(provider)
                .providerUserId(providerUserId).build();

        user.addSocialAccount(socialAccount);
        userSocialAccountRepository.save(socialAccount);

        return user;
    }

}
