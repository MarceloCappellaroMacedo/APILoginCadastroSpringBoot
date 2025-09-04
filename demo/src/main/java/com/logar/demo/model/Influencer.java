package com.logar.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "influencers")
public class Influencer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    @Column(nullable = false, length = 255)
    private String nome;

    @NotBlank(message = "Perfil do Instagram é obrigatório")
    @Size(max = 255, message = "Perfil do Instagram deve ter no máximo 255 caracteres")
    @Column(name = "perfil_instagram", nullable = false, unique = true, length = 255)
    private String perfilInstagram;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Size(max = 255, message = "Email deve ter no máximo 255 caracteres")
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, max = 255, message = "Senha deve ter entre 6 e 255 caracteres")
    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String senha;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro = LocalDateTime.now();

    // --- Getters e Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPerfilInstagram() {
        return perfilInstagram;
    }

    public void setPerfilInstagram(String perfilInstagram) {
        this.perfilInstagram = perfilInstagram;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
    }

}
