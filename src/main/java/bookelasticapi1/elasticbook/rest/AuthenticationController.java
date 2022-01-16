package bookelasticapi1.elasticbook.rest;

import java.util.List;
import java.util.stream.Collectors;
import bookelasticapi1.elasticbook.config.JwtTokenProvider;
import bookelasticapi1.elasticbook.dto.AuthUser;
import bookelasticapi1.elasticbook.dto.LoginDto;
import bookelasticapi1.elasticbook.dto.UserDto;
import bookelasticapi1.elasticbook.model.sql.Role;
import bookelasticapi1.elasticbook.model.sql.User;
import bookelasticapi1.elasticbook.service.UserOwnedBooksIndexService;
import bookelasticapi1.elasticbook.service.base.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserService userService;

    private final UserOwnedBooksIndexService elasticsearchUserService;

    @Autowired
    public AuthenticationController(final AuthenticationManager authenticationManager,
                                    final JwtTokenProvider jwtTokenProvider,
                                    final UserService userService,
                                    final UserOwnedBooksIndexService elasticsearchUserService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.elasticsearchUserService = elasticsearchUserService;
    }

    @PostMapping(value = "/authenticate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> generateToken(@RequestBody LoginDto loginDto) throws AuthenticationException {

        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String token = jwtTokenProvider.generateToken(authentication);

        final User user = userService.findByUsername(authentication.getName());
        List<String> roles = user.getRoles().stream()
                                            .map(Role::getName)
                                            .collect(Collectors.toList());

        return ResponseEntity.ok(new AuthUser(token,
                                user.getId(),
                                user.getUsername(),
                                roles));
    }

    @PostMapping("/register")
    public ResponseEntity<?> saveUser(@RequestBody UserDto user) {
        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        final User newUser = userService.save(user);
        elasticsearchUserService.save(newUser);

        return ResponseEntity.ok("User registered successfully!");
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/userping")
    public String userPing(){
        return "Users can read this";
    }
}
