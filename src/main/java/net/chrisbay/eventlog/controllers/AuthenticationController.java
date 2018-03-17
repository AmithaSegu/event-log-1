package net.chrisbay.eventlog.controllers;

import net.chrisbay.eventlog.user.EmailExistsException;
import net.chrisbay.eventlog.user.UserDto;
import net.chrisbay.eventlog.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.security.Principal;

/**
 * Created by Chris Bay
 */
@Controller
public class AuthenticationController extends AbstractBaseController {

    @GetMapping(value = "/register")
    public String registerForm(Model model) {
        model.addAttribute(new UserDto());
        model.addAttribute("title", "Register");
        return "register";
    }

    @PostMapping(value = "/register")
    public String register(@ModelAttribute @Valid UserDto userDto, Errors errors) {

        if (errors.hasErrors())
            return "register";

        try {
            userService.save(userDto);
        } catch (EmailExistsException e) {
            errors.rejectValue("email", "email.alreadyexists", e.getMessage());
            return "register";
        }

        return "redirect:/welcome";
    }

    @GetMapping(value = "/login")
    public String login(Principal user, Model model, String error, String logout) {

        if (user != null)
            return "redirect:/welcome";

        if (error != null)
            model.addAttribute("errorMsg", "Your username and password are invalid.");

        if (logout != null)
            model.addAttribute("msg", "You have been logged out successfully.");



        return "login";
    }

}
