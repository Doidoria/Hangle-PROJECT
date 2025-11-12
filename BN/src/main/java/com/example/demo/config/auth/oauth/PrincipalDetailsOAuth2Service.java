package com.example.demo.config.auth.oauth;

import com.example.demo.config.auth.oauth.provider.GoogleUserInfo;
import com.example.demo.config.auth.oauth.provider.KakaoUserInfo;
import com.example.demo.config.auth.oauth.provider.NaverUserInfo;
import com.example.demo.config.auth.oauth.provider.OAuth2UserInfo;
import com.example.demo.config.auth.service.PrincipalDetails;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class PrincipalDetailsOAuth2Service extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        //OAuth2UserInfo
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("oAuth2User : " + oAuth2User);
        System.out.println("getAttributes : " + oAuth2User.getAttributes());

        OAuth2UserInfo oAuth2UserInfo = null;
        //'kakao','naver','google','in-'
        String provider = userRequest.getClientRegistration().getRegistrationId();

        Map<String,Object> attributes = oAuth2User.getAttributes();
        if(provider.startsWith("kakao")) {
            //카카오 로그인시
            Long id = (Long)attributes.get("id");
            Map<String,Object> properties = (Map<String,Object>)attributes.get("properties");
            Map<String,Object> kakao_account = (Map<String,Object>) attributes.get("kakao_account");
            LocalDateTime connectedAt = attributes.get("connected_at") != null
                    ? OffsetDateTime.parse(attributes.get("connected_at").toString()).toLocalDateTime()
                    : null;
            oAuth2UserInfo = new KakaoUserInfo(id, connectedAt, properties, kakao_account);
            System.out.println("id :" + id);
            System.out.println("connected_at :" + connectedAt);
            System.out.println("properties :" + properties);
            System.out.println("kakao_account :" + kakao_account);
            oAuth2UserInfo = new KakaoUserInfo(id,connectedAt,properties,kakao_account);

        }else if(provider.startsWith("naver")){
            //네이버 로그인시
            Map<String,Object> response = (Map<String,Object>)attributes.get("response");
            String id = (String)response.get("id");
            oAuth2UserInfo = new NaverUserInfo(id,response);

        }else if(provider.startsWith("google")){
            String id = (String)attributes.get("sub");
            oAuth2UserInfo = new GoogleUserInfo(id,attributes);
        }

        //구글 로그인시
        System.out.println("oAuth2UserInfo : " + oAuth2UserInfo);

        //최초 로그인시 로컬계정 DB 저장 처리
        String username = oAuth2UserInfo.getName();
//        String userid = oAuth2UserInfo.getProvider()+"_"+oAuth2UserInfo.getProviderId();
        String providerId = oAuth2UserInfo.getProviderId();

        String userid = null;
        if (provider.startsWith("kakao")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null && kakaoAccount.get("email") != null) {
                userid = kakaoAccount.get("email").toString();
            }
        } else if (provider.startsWith("naver")) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response != null && response.get("email") != null) {
                userid = response.get("email").toString();
            }
        } else if (provider.startsWith("google")) {
            if (attributes.get("email") != null) {
                userid = attributes.get("email").toString();
            }
        }
        if (userid == null || userid.isBlank()) {
            userid = provider + "_" + providerId; // 이메일 미제공 대비 안전한 fallback
        }

        String password = passwordEncoder.encode("1234");
        User user = userRepository.findByUserid(userid);
        if(user == null){
            //최초 로그인(Dto , Entity)
            user = User.builder()
                    .userid(userid)
                    .password(password)
                    .role("ROLE_USER")
                    .username(username != null ? username : provider + "_user")
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(user);  //계정 등록
            System.out.println("[OAuth2] 신규 사용자 등록 완료 → " + userid);
        }else{
            // 기존 사용자: provider 정보 최신화
            user.setProvider(provider);
            user.setProviderId(providerId);
            userRepository.save(user);
            System.out.println("[OAuth2] 기존 사용자 로그인 → " + userid);
        }
        return new PrincipalDetails(user, attributes);
    }
}
