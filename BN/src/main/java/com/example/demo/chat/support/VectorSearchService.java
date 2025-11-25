/*
============================================================
VectorSearchService.java 전체 용도
============================================================
- OpenAPI 문서를 파싱하여 API 엔드포인트를 "문서 객체(Document)"로 변환하고
 Cosine Similarity 기반 검색(Keywords → API 자동 매핑)을 수행하는 핵심 검색 엔진.
- 기능 요약:
 1) OpenAPI JSON → Document 리스트(엔드포인트 단위)로 인덱싱
 2) 각 Document를 Bag-of-Words 기반 벡터로 변환
 3) Cosine Similarity 로 검색 질의(query)와 문서(Document)를 비교
 4) 최소 유사도 기준(MIN_SIMILARITY)에 따라 API 후보 필터링
 5) 가장 유사한 API 엔드포인트 top-K 반환
- Spring AI 기반 챗봇이 "이 API의 기능 알려줘", "로그인 API 어딨어?" 같은 질문에
 자동으로 API 문서를 찾아주는 검색 엔진 역할 수행.

============================================================
각 라인별 상세 주석
============================================================
*/

package com.example.demo.chat.support;                     // 패키지 경로

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component                                                // 스프링 컴포넌트로 등록
public class VectorSearchService {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchService.class);
    // 로깅용 Logger 생성

    private static final Pattern TOKEN_SPLIT = Pattern.compile("\\W+");
    // 비문자(특수문자) 기준으로 토큰 분리하는 정규식

    private static final double MIN_SIMILARITY = 0.08;
    // 검색 최소 유사도 기준 → 너무 높으면 검색 안 됨, 너무 낮으면 노이즈 증가

    private final CosineSimilarity cosineSimilarity = new CosineSimilarity();
    // Apache commons-text 코사인 유사도 계산기

    private final AtomicReference<List<IndexedDocument>> documents = new AtomicReference<>(List.of());
    // 인덱싱된 문서를 스레드 안전하게 보관하는 저장소

    public VectorSearchService(Optional<OpenAPI> openAPI) {
        // Optional<OpenAPI>가 있으면 초기 인덱싱 수행
        openAPI.ifPresent(this::refresh);
    }

    public List<Document> getAllDocuments() {
        // 현재 인덱싱된 모든 문서를 Document 리스트로 반환
        var docs = documents.get();
        return docs.stream()
                .map(IndexedDocument::document)
                .toList();
    }

    public List<Document> findRelatedDocuments(String query, int topK) {
        // 검색어(query)와 가장 유사한 top-K 문서 리스트를 반환
        var docs = documents.get();
        if (docs.isEmpty() || query == null || query.isBlank()) {
            return Collections.emptyList();  // 조건 불충족 시 빈 리스트
        }

        var queryVector = toVector(query);  // 검색어 벡터화
        if (queryVector.isEmpty()) {
            // 검색어에서 의미 있는 토큰이 없을 경우 상위 K개 반환
            return docs.stream()
                    .limit(Math.max(topK, 1))
                    .map(IndexedDocument::document)
                    .toList();
        }

        var scoredResults = docs.stream()
                // 각 문서에 대해 유사도 계산
                .map(doc -> new ScoredDocument(doc.document(), similarity(doc.vector(), queryVector)))
                // 최소 유사도 필터링
                .filter(scored -> scored.score() >= MIN_SIMILARITY)
                // 유사도 내림차순 정렬
                .sorted(Comparator.comparingDouble(ScoredDocument::score).reversed())
                // top-K 개수 제한
                .limit(Math.max(topK, 1))
                .toList();

        if (scoredResults.isEmpty()) {
            // 매칭된 문서 없음 로그
            log.debug("검색어 '{}' - 유사도 {}% 이상인 문서 없음", query, (int)(MIN_SIMILARITY * 100));
            return Collections.emptyList();
        }

        log.debug("검색어 '{}' - {}개 문서 검색됨 (최고 유사도: {}%)",
                query, scoredResults.size(), (int)(scoredResults.get(0).score() * 100));

        // Document만 추출하여 반환
        return scoredResults.stream()
                .map(ScoredDocument::document)
                .toList();
    }

    public void refreshFromJson(String openApiJson) {
        // JSON 문자열을 OpenAPI 객체로 변환한 뒤 인덱싱 수행
        if (openApiJson == null || openApiJson.isBlank()) {
            log.warn("OpenAPI JSON이 비어 있어 문서 인덱싱을 건너뜁니다.");
            return;
        }
        try {
            var options = new ParseOptions();
            options.setResolve(true);          // $ref 해석
            options.setFlatten(true);         // flatten 처리
            SwaggerParseResult result = new OpenAPIV3Parser().readContents(openApiJson, null, options);

            if (result.getMessages() != null && !result.getMessages().isEmpty()) {
                log.warn("OpenAPI 문서 파싱 경고: {}", result.getMessages());
            }
            if (result.getOpenAPI() != null) {
                refresh(result.getOpenAPI());   // 성공 시 인덱싱
            } else {
                log.warn("OpenAPI 문서를 파싱했지만 유효한 객체가 생성되지 않았습니다.");
            }
        } catch (Exception ex) {
            log.error("OpenAPI JSON 파싱에 실패했습니다.", ex);
        }
    }

    public void refresh(OpenAPI openAPI) {
        // OpenAPI 객체로부터 문서를 인덱싱하는 메인 로직
        if (openAPI == null) {
            return;
        }
        var built = buildDocuments(openAPI);      // Document 리스트 생성
        documents.set(built);                     // 인덱싱 저장
        log.info("OpenAPI 문서를 기반으로 {}개의 엔드포인트 문서를 인덱싱했습니다.", built.size());
    }

    private List<IndexedDocument> buildDocuments(OpenAPI openAPI) {
        // OpenAPI의 paths 를 순회하며 Document 리스트 생성
        if (openAPI.getPaths() == null || openAPI.getPaths().isEmpty()) {
            return List.of();
        }

        List<IndexedDocument> result = new ArrayList<>();
        openAPI.getPaths().forEach((path, pathItem) -> {
            // pathItem 내 모든 HTTP 메서드 처리
            pathItem.readOperationsMap().forEach((method, operation) -> {
                var content = describeOperation(path, method, operation);
                if (!content.isBlank()) {
                    // 문서 객체 생성 및 벡터 저장
                    result.add(new IndexedDocument(
                            new Document(
                                    content,
                                    method != null ? method.name() : null,
                                    path,
                                    operation != null ? operation.getSummary() : null,
                                    operation != null ? operation.getDescription() : null),
                            toVector(content)));
                }
            });
        });
        return result;
    }

    private String describeOperation(String path, PathItem.HttpMethod method, Operation operation) {
        // API 한 개 엔드포인트를 구조적인 텍스트 설명으로 생성
        var builder = new StringBuilder();
        builder.append("[").append(method.name()).append("] ").append(path).append('\n');

        var pathKeywords = extractPathKeywords(path);  // /auth/login → auth login
        if (!pathKeywords.isEmpty()) {
            builder.append("키워드: ").append(pathKeywords).append('\n');
        }

        if (operation.getSummary() != null) {
            builder.append("요약: ").append(operation.getSummary()).append('\n');
        }
        if (operation.getDescription() != null) {
            builder.append("설명: ").append(operation.getDescription()).append('\n');
        }

        // 요청 파라미터
        if (operation.getParameters() != null && !operation.getParameters().isEmpty()) {
            builder.append("요청 파라미터:\n");
            for (Parameter parameter : operation.getParameters()) {
                builder.append(" - ")
                        .append(parameter.getName())
                        .append(" (in: ").append(parameter.getIn()).append(')');
                if (Boolean.TRUE.equals(parameter.getRequired())) {
                    builder.append(" [required]");
                }
                if (parameter.getDescription() != null) {
                    builder.append(" : ").append(parameter.getDescription());
                }
                builder.append('\n');
            }
        }

        // 요청 본문 내용
        if (operation.getRequestBody() != null && operation.getRequestBody().getDescription() != null) {
            builder.append("요청 본문: ").append(operation.getRequestBody().getDescription()).append('\n');
        }

        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            var mediaTypes = operation.getRequestBody().getContent().keySet();
            if (!mediaTypes.isEmpty()) {
                builder.append("요청 Content-Type: ").append(String.join(", ", mediaTypes)).append('\n');
            }
        }

        // 응답 코드
        if (operation.getResponses() != null && !operation.getResponses().isEmpty()) {
            builder.append("응답 코드:\n");
            for (Map.Entry<String, ApiResponse> entry : operation.getResponses().entrySet()) {
                builder.append(" - ").append(entry.getKey());
                var apiResponse = entry.getValue();
                if (apiResponse.getDescription() != null) {
                    builder.append(" : ").append(apiResponse.getDescription());
                }
                builder.append('\n');
            }
        }

        // 태그
        var tags = operation.getTags();
        if (tags != null && !tags.isEmpty()) {
            builder.append("태그: ").append(String.join(", ", tags)).append('\n');
        }

        // 응답 스키마 파싱
        var responses = operation.getResponses();
        if (responses != null) {
            var schemas = responses.values().stream()
                    .map(ApiResponse::getContent)
                    .filter(Objects::nonNull)
                    .flatMap(content -> content.values().stream())
                    .map(media -> media.getSchema())
                    .filter(Objects::nonNull)
                    .map(this::describeSchema)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());

            if (!schemas.isEmpty()) {
                builder.append("응답 스키마: ").append(String.join(" | ", schemas)).append('\n');
            }
        }

        return builder.toString().trim();
    }

    private Optional<String> describeSchema(Schema<?> schema) {
        // 스키마의 타입 혹은 $ref를 텍스트로 변환
        if (schema == null) {
            return Optional.empty();
        }
        var type = schema.getType();
        var ref = schema.get$ref();
        if (ref != null) {
            return Optional.of("ref=" + ref);
        }
        if (type != null) {
            return Optional.of("type=" + type);
        }
        return Optional.empty();
    }

    private String extractPathKeywords(String path) {
        // API path에서 의미 있는 세그먼트만 추출 (auth, login 등)
        if (path == null || path.isBlank()) {
            return "";
        }
        var segments = new ArrayList<String>();
        for (var segment : path.split("/")) {
            if (segment.isBlank() || segment.equals("api") || segment.equals("v1") ||
                    segment.equals("demo") || segment.startsWith("{")) {
                continue;
            }
            segments.add(segment);
        }
        return String.join(" ", segments);
    }

    private Map<CharSequence, Integer> toVector(String text) {
        // 텍스트를 단어별 Bag-of-Words 벡터로 변환
        if (text == null || text.isBlank()) {
            return Map.of();
        }
        var vector = new HashMap<CharSequence, Integer>();
        for (String token : TOKEN_SPLIT.split(text.toLowerCase(Locale.ROOT))) {
            if (token.isBlank() || token.length() < 2) {
                continue; // 너무 짧은 단어 필터링
            }
            vector.merge(token, 1, Integer::sum); // 단어 빈도수 증가
        }
        return vector;
    }

    private double similarity(Map<CharSequence, Integer> left, Map<CharSequence, Integer> right) {
        // 두 벡터의 코사인 유사도 계산
        if (left.isEmpty() || right.isEmpty()) {
            return 0.0;
        }
        return cosineSimilarity.cosineSimilarity(left, right);
    }

    // 문서 데이터 구조: 텍스트 + 메타정보
    public record Document(String content, String httpMethod, String path, String summary, String description) {}

    // 인덱싱된 문서(문서 + 벡터)
    private record IndexedDocument(Document document, Map<CharSequence, Integer> vector) {}

    // 유사도 계산 결과(문서 + 점수)
    private record ScoredDocument(Document document, double score) {}
}


