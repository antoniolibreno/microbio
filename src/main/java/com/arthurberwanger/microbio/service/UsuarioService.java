package com.arthurberwanger.microbio.service;

import com.arthurberwanger.microbio.model.Cliente;
import com.arthurberwanger.microbio.model.Endereco;
import com.arthurberwanger.microbio.model.Usuario;
import com.arthurberwanger.microbio.repository.ClienteRepository;
import com.arthurberwanger.microbio.repository.EnderecoRepository;
import com.arthurberwanger.microbio.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          ClienteRepository clienteRepository,
                          EnderecoRepository enderecoRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ── Listar todos ──────────────────────────────────────────────────
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // ── Buscar por ID ─────────────────────────────────────────────────
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
    }

    // ── Criar apenas usuário (login + senha) ──────────────────────────
    @Transactional
    public void criarUsuarioSimples(String login, String senha) {
        if (usuarioRepository.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException("Login '" + login + "' já está em uso.");
        }
        usuarioRepository.save(new Usuario(login, passwordEncoder.encode(senha)));
    }

    // ── Criar usuário + cliente + endereço ────────────────────────────
    @Transactional
    public void criarUsuarioCompleto(String login, String senha,
                                     String tipoCliente, String cpfCnpj,
                                     String rua, String numero, String bairro,
                                     String cidade, String estado, String cep) {
        if (usuarioRepository.findByLogin(login).isPresent()) {
            throw new IllegalArgumentException("Login '" + login + "' já está em uso.");
        }
        if (cpfCnpj != null && !cpfCnpj.isBlank() && clienteRepository.existsByCpfCnpj(cpfCnpj)) {
            throw new IllegalArgumentException("CPF/CNPJ '" + cpfCnpj + "' já cadastrado.");
        }

        Endereco endereco = new Endereco();
        endereco.setRua(rua);
        endereco.setNumero(numero);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setEstado(estado);
        endereco.setCep(cep);
        enderecoRepository.save(endereco);

        Cliente cliente = new Cliente();
        cliente.setTipoCliente(tipoCliente);
        cliente.setCpfCnpj(cpfCnpj);
        cliente.setEndereco(endereco);
        clienteRepository.save(cliente);

        usuarioRepository.save(new Usuario(login, passwordEncoder.encode(senha)));
    }

    // ── Atualizar login e/ou senha ────────────────────────────────────
    @Transactional
    public void atualizar(Long id, String novoLogin, String novaSenha) {
        Usuario usuario = buscarPorId(id);

        // Verifica se o novo login já está em uso por OUTRO usuário
        usuarioRepository.findByLogin(novoLogin).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new IllegalArgumentException("Login '" + novoLogin + "' já está em uso.");
            }
        });

        usuario.setLogin(novoLogin);

        // Só atualiza senha se uma nova foi fornecida
        if (novaSenha != null && !novaSenha.isBlank()) {
            usuario.setSenha(passwordEncoder.encode(novaSenha));
        }

        usuarioRepository.save(usuario);
    }

    // ── Excluir ───────────────────────────────────────────────────────
    @Transactional
    public void excluir(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // ── Contadores para o dashboard ───────────────────────────────────
    public long contarUsuarios() {
        return usuarioRepository.count();
    }
}