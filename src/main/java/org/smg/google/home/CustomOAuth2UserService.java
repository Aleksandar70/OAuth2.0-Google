package org.smg.google.home;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smg.google.home.model.Role;
import org.smg.google.home.model.User;
import org.smg.google.home.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private static final String SUB = "sub";
    private static final String NAME = "name";
    private static final String EMAIL = "email";

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);

            Map<String, Object> attributes = oAuth2User.getAttributes();
            String googleId = (String) attributes.get(SUB);
            String name = (String) attributes.get(NAME);
            String email = (String) attributes.get(EMAIL);

            if (googleId == null) {
                logger.error("Missing 'sub' attribute in Google OAuth2 response");
                throw new OAuth2AuthenticationException("Missing 'sub' attribute in Google OAuth2 response");
            }

            User user = userRepository.findByGoogleId(googleId)
                    .orElseGet(() -> {
                        logger.info("No user found for google id {}", googleId);
                        User newUser = new User();
                        newUser.setGoogleId(googleId);
                        newUser.setName(name);
                        newUser.setEmail(email);
                        newUser.setRole(Role.USER);
                        return newUser;
                    });

            //update user
            user.setName(name);
            user.setEmail(email);

            userRepository.save(user);//save to DB
            logger.info("User saved or updated successfully: {}", user);

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("USER")),
                    attributes,
                    "sub");
        } catch (Exception e) {
            logger.error("Error loading user", e);
            throw new OAuth2AuthenticationException("Failed to process user authentication");
        }

    }
}
