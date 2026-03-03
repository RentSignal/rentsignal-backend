package me.rentsignal.global.security;

import java.util.Map;

/**
 * Provider별로 다른 JSON 구조를 동일 형태로 변환하기 위한 인터페이스
 */
public interface OAuth2UserInfo {

    Map<String, Object> getAttributes();

    String getProvider();

    String getProviderId();

    String getName();

}
