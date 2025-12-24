package pmto._bpm.viaturas.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pmto._bpm.viaturas.auth.repository.UserRepository;
import pmto._bpm.viaturas.exception.RegisterException;

@Service
public class UserService implements UserDetailsService{
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String matricula) throws RegisterException {
        return userRepository.findByMatricula(matricula)
                .orElseThrow(() -> new RegisterException("Usuário não encontrado com matrícula: " + matricula));
    }

}
