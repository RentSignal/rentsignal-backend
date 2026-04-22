package me.rentsignal.data.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rentsignal.global.exception.BaseException;
import me.rentsignal.global.exception.ErrorCode;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public <T> T getResponse(String url, Class<T> responseType) {
        try {
            String responseBody = restTemplate
                    .exchange(url, HttpMethod.GET, null, String.class).getBody();

            if (responseBody == null || responseBody.isBlank())
                throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "외부 API로부터 응답을 받아오지 못했습니다.");

            return objectMapper.readValue(responseBody, responseType);
        } catch (ResourceAccessException e) {
            log.error("외부 API 연결 에러 - " + e.getMessage());
            throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "외부 API 연결에 실패했습니다.");
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("외부 API 에러 - " + e.getMessage());
            throw new BaseException(ErrorCode.EXTERNAL_API_ERROR, "외부 API에서 알 수 없는 오류가 발생했습니다.");
        }
    }

}
