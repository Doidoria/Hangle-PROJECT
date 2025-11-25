/*
============================================================
OpenApiDocumentLoader.java 파일 전체 용도
============================================================
- 애플리케이션 실행 직후(OpenAPI 최초 로딩)와 일정 주기마다(Scheduler)
  OpenAPI 문서를 서버에서 가져와(JSON) 벡터 검색용 데이터로 재구성하는 기능을 수행하는 컴포넌트.
- VectorSearchService 를 통해 OpenAPI 스펙을 벡터화하여
  'Spring AI 기반 챗봇'이 API 관련 질문을 이해하고 답변하도록 지원하는 핵심 로더 역할.

============================================================
각 라인별 상세 주석 버전
============================================================
*/

package com.example.demo.chat.support;                 // 패키지 선언: support 하위 기능 모듈

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component                                              // 스프링 빈 등록 → 자동 탐지 및 사용 가능
public class OpenApiDocumentLoader implements ApplicationListener<ApplicationReadyEvent> { // 애플리케이션 기동 이벤트 감지

    private static final Logger log = LoggerFactory.getLogger(OpenApiDocumentLoader.class);
    // 현재 클래스용 Logger 생성

    private final RestTemplate restTemplate = new RestTemplate();
    // REST API 요청을 보내기 위한 RestTemplate 인스턴스 생성

    private final VectorSearchService vectorSearchService;
    // OpenAPI 문서를 벡터 DB용 데이터로 변환하는 서비스

    private final String openApiUrl;
    // OpenAPI JSON 문서를 가져올 URL을 저장할 변수

    // 생성자: VectorSearchService 주입 + openApiUrl 값을 application.properties 에서 읽어옴
    public OpenApiDocumentLoader(VectorSearchService vectorSearchService,
                                 @Value("${chatbot.open-api-url:http://localhost:${server.port:8080}/v3/api-docs}") String openApiUrl) {
        this.vectorSearchService = vectorSearchService;  // 벡터 검색 서비스 주입
        this.openApiUrl = openApiUrl;                    // 프로퍼티에서 읽은 URL 저장
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 애플리케이션이 완전히 기동되었을 때 자동 실행
        loadOpenApi();  // 최초 OpenAPI 문서 로딩
    }

    // 스케줄러: 최초 지연(initialDelayString) 후 지정 주기(fixedDelayString)마다 실행
    @Scheduled(initialDelayString = "${chatbot.open-api-refresh-initial-delay:PT5M}",
            fixedDelayString = "${chatbot.open-api-refresh-interval:PT30M}")
    public void scheduledReload() {
        loadOpenApi();  // 주기적으로 OpenAPI 문서 다시 로드
    }

    private void loadOpenApi() {
        // OpenAPI 문서를 가져와 벡터 검색 서비스에 반영하는 핵심 메서드
        try {
            log.info("OpenAPI 문서를 {}에서 로드합니다.", openApiUrl);
            // 로딩 시작 로그 출력

            var response = restTemplate.getForObject(openApiUrl, String.class);
            // REST GET 요청으로 OpenAPI JSON 문서 가져오기

            vectorSearchService.refreshFromJson(response);
            // 가져온 JSON 문서를 벡터 검색 시스템에 반영(파싱 + 벡터화)

            log.info("OpenAPI 문서 로드가 완료되었습니다.");
            // 성공 로그 출력
        } catch (Exception ex) {
            log.warn("OpenAPI 문서를 {}에서 로드하는 데 실패했습니다. {}", openApiUrl, ex.getMessage());
            // 오류 발생 시 경고 로그 출력
        }
    }
}



