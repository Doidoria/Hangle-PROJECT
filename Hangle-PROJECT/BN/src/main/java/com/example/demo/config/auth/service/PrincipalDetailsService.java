package com.example.demo.config.auth.service;

import com.example.demo.domain.user.dto.UserDto;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class PrincipalDetailsService implements UserDetailsService{

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
		
		System.out.println("loadUserByUserid .. " + userid);
		Optional<User> userOption  = userRepository.findById(userid);
		if(userOption.isEmpty())
			throw new UsernameNotFoundException(userid + " 존재하지 않는 계정입니다.");

		//entity-> dto
		UserDto userDto = UserDto.toDto(userOption.get());
		return new PrincipalDetails(userDto);
	}

}


