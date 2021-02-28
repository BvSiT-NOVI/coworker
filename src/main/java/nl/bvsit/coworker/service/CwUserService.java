package nl.bvsit.coworker.service;

import nl.bvsit.coworker.config.CwConstants;
import nl.bvsit.coworker.domain.ERole;
import nl.bvsit.coworker.domain.User;
import nl.bvsit.coworker.exceptions.BadRequestException;
import nl.bvsit.coworker.exceptions.RecordNotFoundException;
import nl.bvsit.coworker.repository.RoleRepository;
import nl.bvsit.coworker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class CwUserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    public User validatedUser(Long id, ERole... roles){
        if (id==null) throw new BadRequestException("Error: no user present");
        User user = userRepository.findById(id).orElseThrow(
                () -> new RecordNotFoundException(String.format(CwConstants.RESOURCE_NOT_FOUND_WITH_ID, id)));
        if (roles.length==0) return user;//If no roles are passed assume as validated. NB 'Three dot' variable parameter is always an array
        for(ERole role:roles ){
            if (!roleRepository.existsByName(role)) throw new RuntimeException("Role does not exist");
            if (user.hasRole(role)) return user;
        }
         throw new BadRequestException("User does not have correct role.");
    }

    public User validatedUser(UserDetailsImpl authUser, ERole... roles) {
        if (authUser==null) throw new AccessDeniedException("Access is denied");
        return validatedUser(authUser.getId(),roles);
    }
}
