package com.arthurberwanger.microbio.service;

import com.arthurberwanger.microbio.dto.OrcamentoDTO;
import com.arthurberwanger.microbio.model.Pessoa;
import com.arthurberwanger.microbio.repository.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrcamentoService {

    private final PessoaRepository pessoaRepository;

    public OrcamentoService(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    /**
     * Salva uma solicitação de orçamento feita por um visitante público do site.
     * Ainda não há cliente vinculado — isso será feito pela empresa depois do cadastro completo.
     */
    @Transactional
    public Pessoa registrarSolicitacao(OrcamentoDTO dto) {
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(dto.getNome().trim());
        pessoa.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        pessoa.setTelefone(dto.getTelefone() != null ? dto.getTelefone().trim() : null);
        pessoa.setTipoServico(dto.getTipoServico());
        // cliente fica null — será vinculado após cadastro completo pela empresa
        return pessoaRepository.save(pessoa);
    }

    public List<Pessoa> listarTodas() {
        return pessoaRepository.findAllComCliente();
    }
}