package pl.coderslab.controller;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.entity.Tweet;
import pl.coderslab.entity.User;
import pl.coderslab.repository.TweetRepository;
import pl.coderslab.repository.UserRepository;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping(path = "user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TweetRepository tweetRepository;

    @GetMapping(path = "login")
    public String showLoginForm() {
        return "user/login";
    }

    @PostMapping(path = "login")
    public String processLoginRequest(@RequestParam("username") String username,
                                      @RequestParam("password") String password,
                                      HttpSession session) {
//        User user = userRepository.findOneByUsernameAndPassword(username, password);
        User user = userRepository.findOneByUsername(username);
        if(user != null) {
            String passwordToCheck = user.getPassword();
            boolean isPasswordCorrect = BCrypt.checkpw(password, passwordToCheck);
            if (isPasswordCorrect) {
                session.setAttribute("user", user);
                return "redirect:/";
            } else {
                return "user/login";
            }
        } else {
            return "user/login";
        }
    }

    @GetMapping(path = "logout")
    public String processLogoutRequest(HttpSession session) {
        session.removeAttribute("user");
        return "user/login";
    }

    @GetMapping(path = "register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping(path = "register")
    public String processRegistrationRequest(@Valid User user, BindingResult bindingResult, HttpSession session) {
        if(bindingResult.hasErrors()) {
            return "user/register";
        } else {
            userRepository.save(user);
            session.setAttribute("user", user);
            return "redirect:/";
        }
    }

    //TODO write settings functionality (e.g. edit password)
    @GetMapping(path = "settings")
    public String showUserSettingsPage(Model model,
                                       @SessionAttribute(name = "user", required = false) User user) {
        if(user != null) {
            model.addAttribute("user", user);
            return "user/settings";
        } else {
            return "user/login";
        }
    }

    @GetMapping(path = "me")
    public String userSite(@SessionAttribute(name = "user", required = false) User user, Model model) {
        List<Tweet> tweets = tweetRepository.getTweetsByUserIdUsingQuery(user.getId());
        model.addAttribute("tweets", tweets);
        return "tweet/list";
    }

    @GetMapping(path = "show")
    public String show(@SessionAttribute(name = "user", required = false) User user, Model model,
                       @RequestParam("id") long id, HttpSession session) {
        if (user != null) {
            if (user.getId() == id) {
                return "redirect:/user/me";
            } else {
                List<Tweet> tweets = tweetRepository.getTweetsByUserIdUsingQuery(id);
                String username = userRepository.findOneById(id).getUsername();
                User receiver = userRepository.findOneById(id);
                session.setAttribute("receiver", receiver);
                model.addAttribute("username", username);
                model.addAttribute("tweets", tweets);
                return "tweet/show";
            }
        } else {
            return "user/login";
        }
    }

}
