package com.example.demo.config.auth;

import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Service
@Slf4j
public class PrincipalDetailsService implements UserDetailsService{

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		System.out.println("loadUserByUsername .. " + username);
		Optional<User> userOption  = userRepository.findById(username);
		if(userOption.isEmpty())
			throw new UsernameNotFoundException(username + " 존재하지 않는 계정입니다.");

		//entity-> dto
		UserDto userDto = UserDto.toDto( userOption.get()    );
		return new PrincipalDetails(userDto);
	}

}


