package com.app.mvc;

import com.app.entity.Users;
import com.app.entity.Role;
import com.app.form.PasswordForm;
import com.app.form.UserForm;
import com.app.repos.RoleRepository;
import com.app.repos.UsersRepository;
import com.app.service.AdminService;
import com.app.validator.PasswordValidator;
import com.app.validator.UserFormValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("admin")
@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
public class AdminController {
    @Autowired
    UsersRepository userRepository;

    @Autowired
    AdminService adminService;

    @Autowired
    RoleRepository roleRepository;


    @Autowired
    private PasswordValidator passwordValidator;
    @Autowired
    private UserFormValidator userFormValidator;

    @InitBinder
    protected void initBinder(WebDataBinder dataBinder) {
        // Form target
        Object target = dataBinder.getTarget();
        if (target == null) {
            return;
        }

        if (target.getClass() == PasswordForm.class) {
            dataBinder.setValidator(passwordValidator);
        }
        if (target.getClass() == UserForm.class) {
            dataBinder.setValidator(userFormValidator);
        }
    }


    @RequestMapping(method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("users",userRepository.findAll());
        //model.addAttribute("roles",roleRepository.findAll());
        return "admin/index";
    }

    @RequestMapping("/registerSuccessful")
    public String viewRegisterSuccessful(Model model) {
        return "/admin/registerSuccessfulPage";
    }





    @RequestMapping(value = "/user/{login}", method = RequestMethod.GET)
    public String editUser(Model model, @PathVariable String login, Principal principal) {

        Optional<Users> appUser = userRepository.findById(login);
        if (appUser.isPresent())
        {
            UserForm userForm = new UserForm(appUser.get());
            List<Role> roleList = roleRepository.findAll();
            roleList.removeIf(r->r.getName().equals("ROLE_ADMIN"));
            model.addAttribute("user", userForm);
            model.addAttribute("roles", roleList);
            return "admin/user";

        }

        return "redirect:/admin";
    }


    @RequestMapping(value = "/resetPassword/{login}", method = RequestMethod.GET)
    public String ressetPassword(Model model, @PathVariable String login, Principal principal) {
        if (login.isEmpty()) {
            login = principal.getName();
        }

        PasswordForm form = new PasswordForm(login);
        model.addAttribute("passForm", form);
        return "admin/resetPassword";
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.GET)
    public String ressetAdminPassword(Model model,  Principal principal) {
        String  login = principal.getName();
        PasswordForm form = new PasswordForm(login);
        model.addAttribute("passForm", form);
        return "admin/resetPassword";
    }




    @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
    public String ressetingPassword(Model model, //
                                    @ModelAttribute("passForm") @Validated PasswordForm passForm, //
                                    BindingResult result, //
                                    final RedirectAttributes redirectAttributes) {
        // Validate result
        if (result.hasErrors()) {
            return "admin/resetPassword";
        }
        String error = adminService.resetPassword(passForm.getLogin(), passForm.getPassword());
        if (error != null && !error.isEmpty()) {

            model.addAttribute("errorMessage", "Error: " + error);
            return "admin/resetPassword";
        }
        Users user = userRepository.findById(passForm.getLogin()).orElse(null);
        redirectAttributes.addFlashAttribute("users", user);
        return "redirect:/admin";
    }


}
