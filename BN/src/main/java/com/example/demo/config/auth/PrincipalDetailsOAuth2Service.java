package com.example.demo.config.auth;

import com.example.demo.config.auth.provider.GoogleUserInfo;
import com.example.demo.config.auth.provider.KakaoUserInfo;
import com.example.demo.config.auth.provider.NaverUserInfo;
import com.example.demo.config.auth.provider.OAuth2UserInfo;
import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class PrincipalDetailsOAuth2Service extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        System.out.println("PrincipalDetailsOAuth2Service's loadUser invoke..");
//        System.out.println("userRequest : " + userRequest);
//        System.out.println("userRequest.getClientRegistration() :"+ userRequest.getClientRegistration());
//        System.out.println("userRequest.getAccessToken() : "+ userRequest.getAccessToken());
//        System.out.println("userRequest.getAdditionalParameters() : "+ userRequest.getAdditionalParameters());
//        System.out.println("userRequest.getAccessToken().getTokenValue() : "+ userRequest.getAccessToken().getTokenValue());
//        System.out.println("userRequest.getAccessToken().getTokenType().getValue() : "+ userRequest.getAccessToken().getTokenType().getValue());
//        System.out.println("userRequest.getAccessToken().getScopes() : "+ userRequest.getAccessToken().getScopes());

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
            LocalDateTime connected_at = OffsetDateTime.parse( attributes.get("connected_at").toString() ).toLocalDateTime();
            Map<String,Object> properties = (Map<String,Object>)attributes.get("properties");
            Map<String,Object> kakao_account = (Map<String,Object>) attributes.get("kakao_account");
            System.out.println("id :" + id);
            System.out.println("connected_at :" + connected_at);
            System.out.println("properties :" + properties);
            System.out.println("kakao_account :" + kakao_account);
            oAuth2UserInfo = new KakaoUserInfo(id,connected_at,properties,kakao_account);

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
        String username = oAuth2UserInfo.getProvider()+"_"+oAuth2UserInfo.getProviderId();
        String password = passwordEncoder.encode("1234");
        Optional<User> userOptional =  userRepository.findById(username);
        //UserDto 생성 (이유 : PrincipalDetails에 포함)
        //UserEntity 생성(이유 : 최초 로그인시 DB 저장용도)
        UserDto userDto =null;
        if(userOptional.isEmpty()){
            //최초 로그인(Dto , Entity)
            userDto = UserDto   .builder()
                                .username(username)
                                .password(password)
                                .role("ROLE_USER")
                                .build();
            User user = userDto.toEntity();
            userRepository.save(user);  //계정 등록

        }else{
            //기존 유저 존재(Dto)
            User user = userOptional.get();
            userDto = UserDto.toDto(user);
        }


        // PrincipalDetails 전달
        PrincipalDetails principalDetails = new PrincipalDetails();
        userDto.setProvider(provider);
        userDto.setProviderId(oAuth2UserInfo.getProviderId());
        principalDetails.setUserDto(userDto);
        principalDetails.setAttributes(oAuth2User.getAttributes());
        principalDetails.setAccess_token(userRequest.getAccessToken().getTokenValue());
        return principalDetails;

    }
}
