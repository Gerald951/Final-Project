package ibf2022.batch2.miniProject.server.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

import ibf2022.batch2.miniProject.server.model.ERole;
import ibf2022.batch2.miniProject.server.model.Role;
import ibf2022.batch2.miniProject.server.model.User;
import ibf2022.batch2.miniProject.server.payload.response.MessageResponse;
import ibf2022.batch2.miniProject.server.payload.response.UserInfoResponse;
import ibf2022.batch2.miniProject.server.repositories.RoleRepository;
import ibf2022.batch2.miniProject.server.repositories.UserRepository;
import ibf2022.batch2.miniProject.server.security.jwt.JwtUtils;
import ibf2022.batch2.miniProject.server.security.services.UserDetailsImpl;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
@Controller
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @PostMapping(path = "/signin", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> authenticateUser(@RequestBody MultiValueMap<String, String> signInForm) {
    String username = signInForm.getFirst("username");
    String password = signInForm.getFirst("password");

    Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(username, password));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    System.out.println(userDetails);

    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
    System.out.println(jwtCookie.toString());

    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());
        
    System.out.println(roles);
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
        .body(new UserInfoResponse(userDetails.getId(),
                                   userDetails.getUsername(),
                                   userDetails.getEmail(),
                                   roles));
  }

  @PostMapping(path = "/signup", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> registerUser(@RequestBody MultiValueMap<String, String> signUpForm) {
    String username = signUpForm.getFirst("username");
    String password = signUpForm.getFirst("password");
    String email = signUpForm.getFirst("email");
   
    if (userRepository.existsByUsername(username)) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(email)) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }
    
    // Create new user's account
    User user = new User(username,
                         email ,
                         encoder.encode(password));
    
    Set<Role> roles = new HashSet<>();
    
    
    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
    roles.add(userRole);
  
    
    user.setRoles(roles);
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  @PostMapping(path= "/signout", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity<?> logoutUser() {
    ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(new MessageResponse("You've been signed out!"));
  }
}
