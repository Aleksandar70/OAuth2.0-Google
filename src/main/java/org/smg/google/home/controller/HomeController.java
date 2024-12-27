package org.smg.google.home.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;
import java.util.Map;

import static org.smg.google.home.constants.Constants.GUEST;
import static org.smg.google.home.constants.Constants.HOME_PAGE;
import static org.smg.google.home.constants.Constants.LOGIN_PAGE;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final OAuth2AuthorizedClientService authorizedClientService;

    public HomeController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        String username = (authentication != null) ? authentication.getName() : GUEST;

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("google", username);

        if (client != null) {
            String accessToken = client.getAccessToken().getTokenValue();
            model.addAttribute("accessToken", accessToken);
        }

        model.addAttribute("username", username);
        logger.info("Rendering home page for user: {}", username);

        return HOME_PAGE;//Resolves to a view named "home.html" or "home.jsp"
    }

    @GetMapping("/login")
    public String login() {
        logger.info("Rendering login page");

        return LOGIN_PAGE;//Resolves to a view named "login.html" or "login.jsp"
    }

    @GetMapping("/api")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)//Explicitly return OK HTTP response code
    public Map<String, String> api() {
        logger.info("API endpoint accessed");

        return Collections.singletonMap("message", "Hello, Api");//This makes it RESTful and aligns with expected practices in APIs
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/home";
    }
}
