package com.arthurberwanger.microbio.service;

import com.arthurberwanger.microbio.model.AmostraNecessaria;
import com.arthurberwanger.microbio.model.Analise;
import com.arthurberwanger.microbio.repository.AnaliseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnaliseService {

    private final AnaliseRepository repository;

    public AnaliseService(AnaliseRepository repository) {
        this.repository = repository;
    }

    public List<Analise> listarTodas() {
        return repository.findAll();
    }

    public Analise buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Análise #" + id + " não encontrada"));
    }

    public long contarPorStatus(String status) {
        return repository.countByStatus(status);
    }

    @Transactional
    public Analise criar(String nome, String descricao, BigDecimal valor,
                         String tempoProducao, String status,
                         List<String> tipos, List<String> quantidades, List<String> conservacoes) {
        Analise a = new Analise();
        a.setNome(nome);
        a.setDescricao(descricao);
        a.setValor(valor);
        a.setTempoProducao(tempoProducao);
        a.setStatus(status == null ? "ATIVA" : status);
        a.setDataCriacao(LocalDateTime.now());
        a.setDataAtualizacao(LocalDateTime.now());
        a.setAmostras(montarAmostras(a, tipos, quantidades, conservacoes));
        return repository.save(a);
    }

    @Transactional
    public Analise atualizar(Long id, String nome, String descricao, BigDecimal valor,
                             String tempoProducao, String status,
                             List<String> tipos, List<String> quantidades, List<String> conservacoes) {
        Analise a = buscarPorId(id);
        a.setNome(nome);
        a.setDescricao(descricao);
        a.setValor(valor);
        a.setTempoProducao(tempoProducao);
        if (status != null && !status.isBlank()) a.setStatus(status);
        a.setDataAtualizacao(LocalDateTime.now());
        a.getAmostras().clear();
        a.getAmostras().addAll(montarAmostras(a, tipos, quantidades, conservacoes));
        return repository.save(a);
    }

    @Transactional
    public void excluir(Long id) {
        if (!repository.existsById(id))
            throw new EntityNotFoundException("Análise #" + id + " não encontrada");
        repository.deleteById(id);
    }

    private List<AmostraNecessaria> montarAmostras(Analise a, List<String> tipos,
                                                    List<String> quantidades, List<String> conservacoes) {
        List<AmostraNecessaria> result = new ArrayList<>();
        if (tipos == null) return result;
        for (int i = 0; i < tipos.size(); i++) {
            String tipo = tipos.get(i);
            String qtd  = quantidades != null && i < quantidades.size() ? quantidades.get(i) : null;
            String con  = conservacoes != null && i < conservacoes.size() ? conservacoes.get(i) : null;
            boolean vazio = (tipo == null || tipo.isBlank())
                         && (qtd == null || qtd.isBlank())
                         && (con == null || con.isBlank());
            if (vazio) continue;
            AmostraNecessaria am = new AmostraNecessaria();
            am.setTipo(tipo);
            am.setQuantidade(qtd);
            am.setConservacao(con);
            am.setAnalise(a);
            result.add(am);
        }
        return result;
    }
}
