package com.logar.demo.controller;

import com.logar.demo.model.InfluencerResponse;
import com.logar.demo.model.LoginDTO;
import com.logar.demo.model.Influencer;
import com.logar.demo.security.JwtUtil;
import com.logar.demo.repository.InfluencerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/influencers")
public class InfluencerController {

    @Autowired
    private InfluencerRepository influencerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil; // ✅ injeta o utilitário de JWT

    private InfluencerResponse toResponse(Influencer influencer) {
        return new InfluencerResponse(
            influencer.getId(),
            influencer.getNome(),
            influencer.getEmail(),
            influencer.getPerfilInstagram(),
            influencer.getDataCadastro()
        );
    }

    // Criar influencer (POST)
    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody Influencer influencer) {
        try {
            if (influencer.getSenha() == null || influencer.getSenha().isBlank()) {
                return ResponseEntity.badRequest().body("Senha é obrigatória");
            }
            
            // Check if email already exists
            if (influencerRepository.findByEmail(influencer.getEmail()) != null) {
                return ResponseEntity.badRequest().body("Email já está em uso");
            }
            
            // Check if Instagram profile already exists
            if (influencerRepository.findByPerfilInstagram(influencer.getPerfilInstagram()) != null) {
                return ResponseEntity.badRequest().body("Perfil do Instagram já está em uso");
            }
            
            influencer.setSenha(passwordEncoder.encode(influencer.getSenha()));
            Influencer salvo = influencerRepository.save(influencer);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(salvo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao cadastrar influencer: " + e.getMessage());
        }
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO login) {
        Optional<Influencer> optionalInfluencer = Optional.ofNullable(influencerRepository.findByEmail(login.email()));

        if (optionalInfluencer.isPresent()) {
            Influencer influencer = optionalInfluencer.get();
            if (passwordEncoder.matches(login.senha(), influencer.getSenha())) {
                String token = jwtUtil.generateToken(influencer.getEmail());

                return ResponseEntity.ok(
                    Map.of(
                        "token", token,
                        "user", toResponse(influencer)
                    )
                );
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
    }

    // Listar todos
    @GetMapping
    public List<InfluencerResponse> getAllInfluencers() {
        return influencerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Buscar por id
    @GetMapping("/{id}")
    public ResponseEntity<InfluencerResponse> getInfluencerById(@PathVariable Long id) {
        return influencerRepository.findById(id)
                .map(influencer -> ResponseEntity.ok(toResponse(influencer)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Atualizar
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInfluencer(@PathVariable Long id, @Valid @RequestBody Influencer updated) {
        return influencerRepository.findById(id).map(influencer -> {
            try {
                // Check if email is being changed and if new email already exists
                if (!influencer.getEmail().equals(updated.getEmail()) && 
                    influencerRepository.findByEmail(updated.getEmail()) != null) {
                    return ResponseEntity.badRequest().body("Email já está em uso");
                }
                
                // Check if Instagram profile is being changed and if new profile already exists
                if (!influencer.getPerfilInstagram().equals(updated.getPerfilInstagram()) && 
                    influencerRepository.findByPerfilInstagram(updated.getPerfilInstagram()) != null) {
                    return ResponseEntity.badRequest().body("Perfil do Instagram já está em uso");
                }
                
                influencer.setNome(updated.getNome());
                influencer.setPerfilInstagram(updated.getPerfilInstagram());
                influencer.setEmail(updated.getEmail());
                if (updated.getSenha() != null && !updated.getSenha().isBlank()) {
                    influencer.setSenha(passwordEncoder.encode(updated.getSenha()));
                }
                Influencer salvo = influencerRepository.save(influencer);
                return ResponseEntity.ok(toResponse(salvo));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Erro ao atualizar influencer: " + e.getMessage());
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Deletar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInfluencer(@PathVariable Long id) {
        if (influencerRepository.existsById(id)) {
            influencerRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
