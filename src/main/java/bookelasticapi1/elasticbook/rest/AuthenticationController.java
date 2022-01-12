package bookelasticapi1.elasticbook.rest;

import bookelasticapi1.elasticbook.config.JwtTokenProvider;
import bookelasticapi1.elasticbook.dto.LoginDto;
import bookelasticapi1.elasticbook.dto.UserDto;
import bookelasticapi1.elasticbook.model.AuthToken;
import bookelasticapi1.elasticbook.model.sql.User;
import bookelasticapi1.elasticbook.service.UserOwnedBooksIndexService;
import bookelasticapi1.elasticbook.service.UserService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    private final UserService userService;

    private final UserOwnedBooksIndexService esUserService;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserService userService, UserOwnedBooksIndexService esUserService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.esUserService = esUserService;
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
        return ResponseEntity.ok(new AuthToken(token));
    }

    /*@RequestMapping(value="/register", method = RequestMethod.POST)
    public User saveUser(@RequestBody User user){
        return userService.save(user);
    }*/

    @RequestMapping(value="/register", method = RequestMethod.POST)
    public User saveUser(@RequestBody UserDto user){
        final User newUser = userService.save(user);
        esUserService.save(newUser);
        return newUser;
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value="/userping", method = RequestMethod.GET)
    public String userPing(){
        return "Any User Can Read This";
    }
}
