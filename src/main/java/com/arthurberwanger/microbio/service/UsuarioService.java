package com.arthurberwanger.microbio.service;

import com.arthurberwanger.microbio.model.*;
import com.arthurberwanger.microbio.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository  usuarioRepository;
    private final ClienteRepository  clienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final PasswordEncoder    passwordEncoder;

    public UsuarioService(UsuarioRepository u, ClienteRepository c,
                          EnderecoRepository e, PasswordEncoder p) {
        this.usuarioRepository  = u;
        this.clienteRepository  = c;
        this.enderecoRepository = e;
        this.passwordEncoder    = p;
    }

    // Usa findAllComCliente para evitar N+1 e LazyInitializationException na lista
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAllComCliente();
    }

    // Busca simples — só para autenticação interna
    public Usuario buscarPorId(Long id) {
        // Usa JOIN FETCH para carregar cliente+endereço de uma vez
        return usuarioRepository.findByIdComCliente(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + id));
    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    @Transactional
    public void criarUsuarioSimples(String login, String senha) {
        validarLogin(login, null);
        usuarioRepository.save(new Usuario(login, passwordEncoder.encode(senha)));
    }

    @Transactional
    public void criarUsuarioCompleto(String login, String senha,
                                     String tipoCliente, String cpfCnpj,
                                     String rua, String numero, String bairro,
                                     String cidade, String estado, String cep) {
        validarLogin(login, null);
        if (cpfCnpj != null && !cpfCnpj.isBlank() && clienteRepository.existsByCpfCnpj(cpfCnpj))
            throw new IllegalArgumentException("CPF/CNPJ já cadastrado.");

        Endereco endereco = new Endereco();
        preencher(endereco, rua, numero, bairro, cidade, estado, cep);
        enderecoRepository.save(endereco);

        Cliente cliente = new Cliente();
        cliente.setTipoCliente(tipoCliente);
        cliente.setCpfCnpj(cpfCnpj);
        cliente.setEndereco(endereco);
        clienteRepository.save(cliente);

        Usuario u = new Usuario(login, passwordEncoder.encode(senha));
        u.setCliente(cliente);
        usuarioRepository.save(u);
    }

    @Transactional
    public void atualizarAcesso(Long id, String login, String senha) {
        Usuario u = buscarPorId(id);
        validarLogin(login, id);
        u.setLogin(login);
        if (senha != null && !senha.isBlank())
            u.setSenha(passwordEncoder.encode(senha));
        usuarioRepository.save(u);
    }

    @Transactional
    public void atualizarCompleto(Long id, String login, String senha,
                                  String tipoCliente, String cpfCnpj,
                                  String rua, String numero, String bairro,
                                  String cidade, String estado, String cep) {
        Usuario u = buscarPorId(id);
        validarLogin(login, id);
        u.setLogin(login);
        if (senha != null && !senha.isBlank())
            u.setSenha(passwordEncoder.encode(senha));

        Cliente cliente = u.getCliente();
        if (cliente == null) {
            // Usuário ainda não tinha cliente — cria agora
            if (cpfCnpj != null && !cpfCnpj.isBlank() && clienteRepository.existsByCpfCnpj(cpfCnpj))
                throw new IllegalArgumentException("CPF/CNPJ já cadastrado.");
            Endereco e = new Endereco();
            preencher(e, rua, numero, bairro, cidade, estado, cep);
            enderecoRepository.save(e);
            cliente = new Cliente();
            cliente.setTipoCliente(tipoCliente);
            cliente.setCpfCnpj(cpfCnpj);
            cliente.setEndereco(e);
            clienteRepository.save(cliente);
            u.setCliente(cliente);
        } else {
            // Já tem cliente — atualiza campos
            if (cpfCnpj != null && !cpfCnpj.isBlank()
                    && !cpfCnpj.equals(cliente.getCpfCnpj())
                    && clienteRepository.existsByCpfCnpj(cpfCnpj))
                throw new IllegalArgumentException("CPF/CNPJ já cadastrado.");

            cliente.setTipoCliente(tipoCliente);
            cliente.setCpfCnpj(cpfCnpj);

            Endereco e = cliente.getEndereco();
            if (e == null) {
                e = new Endereco();
                enderecoRepository.save(e);
                cliente.setEndereco(e);
            }
            preencher(e, rua, numero, bairro, cidade, estado, cep);
            enderecoRepository.save(e);
            clienteRepository.save(cliente);
        }

        usuarioRepository.save(u);
    }

    @Transactional
    public void excluir(Long id) {
        if (!usuarioRepository.existsById(id))
            throw new IllegalArgumentException("Usuário não encontrado.");
        usuarioRepository.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────
    private void validarLogin(String login, Long idAtual) {
        usuarioRepository.findByLogin(login).ifPresent(outro -> {
            if (!outro.getId().equals(idAtual))
                throw new IllegalArgumentException("Login '" + login + "' já está em uso.");
        });
    }

    private void preencher(Endereco e, String rua, String numero,
                           String bairro, String cidade, String estado, String cep) {
        e.setRua(rua);
        e.setNumero(numero);
        e.setBairro(bairro);
        e.setCidade(cidade);
        e.setEstado(estado);
        e.setCep(cep);
    }
}