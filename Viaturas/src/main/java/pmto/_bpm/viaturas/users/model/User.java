package pmto._bpm.viaturas.users.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pmto._bpm.viaturas.auth.model.Role;
import pmto._bpm.viaturas.batalhao.model.Batalhao;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name= "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeGuerra;


    private Instant ultimaNotificacaoVista;

    private String graduacao;

    @ManyToOne
    @JoinColumn(name = "batalhao_id")
    private Batalhao batalhao;

    private String senha;

    @Column(unique = true)
    private String CPF;

    @Column(unique = true)
    private String matricula;

    @Column(name = "foto_perfil_url")
    private String fotoPerfilUrl;

    @Column(name = "cnh_url")
    private String cnhKey;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User() {
    }

    public Instant getUltimaNotificacaoVista() {
        return ultimaNotificacaoVista;
    }

    public void setUltimaNotificacaoVista(Instant ultimaNotificacaoVista) {
        this.ultimaNotificacaoVista = ultimaNotificacaoVista;
    }

    public String getGraduacao() {
        return graduacao;
    }

    public void setGraduacao(String graduacao) {
        this.graduacao = graduacao;
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    public String getCnhKey() {
        return cnhKey;
    }

    public void setCnhKey(String cnhKey) {
        this.cnhKey = cnhKey;
    }

    public String getNomeGuerra() {
        return nomeGuerra;
    }

    public void setNomeGuerra(String nomeGuerra) {
        this.nomeGuerra = nomeGuerra;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Batalhao getBatalhao() {
        return batalhao;
    }

    public void setBatalhao(Batalhao batalhao) {
        this.batalhao = batalhao;
    }

    public String getCPF() {
        return CPF;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public User(Batalhao batalhao, String nomeGuerra, String senha, String cpf, String matricula, Role role) {
        this.nomeGuerra = nomeGuerra;
        this.senha = senha;
        this.CPF = cpf;
        this.matricula = matricula;
        this.role = role;
        this.batalhao = batalhao;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getPassword() {
        return this.senha; // ou getSenha()
    }

    @Override
    public String getUsername() {
        return this.matricula;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
