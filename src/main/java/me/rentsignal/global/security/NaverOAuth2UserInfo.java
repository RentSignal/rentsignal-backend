package me.rentsignal.global.security;

import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;

import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final Map<String, Object> response;

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        // 네이버는 사용자 정보가 response 안에 들어있음
        Object responseObj = attributes.get("response");
        if (!(responseObj instanceof Map))
            throw new BaseException(ErrorCode.INVALID_SOCIAL_PROVIDER, "네이버 OAuth 응답에 response가 없습니다.");
        this.response = (Map<String, Object>) responseObj;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        Object id = response.get("id");
        if (id == null)
            throw new BaseException(ErrorCode.INVALID_SOCIAL_PROVIDER, "네이버 OAuth response에 id가 없습니다.");
        return id.toString();
    }

    /** nickname 먼저 확인, 없으면 name, 없으면 null
     * -> null일 경우 CustomOAuth2UserService에서 랜덤 닉네임 생성 */
    @Override
    public String getName() {
        Object nickname = response.get("nickname");
        if (nickname != null && !nickname.toString().isBlank())
            return nickname.toString();

        Object name = response.get("name");
        return name == null ? null : name.toString();
    }

}
