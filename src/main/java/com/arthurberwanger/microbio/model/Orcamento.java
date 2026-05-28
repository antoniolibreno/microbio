package com.arthurberwanger.microbio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orcamento")
@Data
@NoArgsConstructor
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "data_orcamento")
    private LocalDateTime dataOrcamento = LocalDateTime.now();

    /**
     * Status do orçamento: PENDENTE → EM_ANDAMENTO → CONCLUIDO | CANCELADO
     * Armazenado como string no banco (coluna status da tabela pedido é usada
     * no Pedido; aqui rastreamos o status do próprio orçamento).
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusOrcamento status = StatusOrcamento.PENDENTE;

    @Column(length = 255)
    private String observacoes;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedido> pedidos = new ArrayList<>();

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrcamentoAnalise> analises = new ArrayList<>();

    public enum StatusOrcamento {
        PENDENTE, EM_ANDAMENTO, CONCLUIDO, CANCELADO;

        public String getLabel() {
            return switch (this) {
                case PENDENTE     -> "Pendente";
                case EM_ANDAMENTO -> "Em andamento";
                case CONCLUIDO    -> "Concluído";
                case CANCELADO    -> "Cancelado";
            };
        }

        public String getCssClass() {
            return switch (this) {
                case PENDENTE     -> "status-pendente";
                case EM_ANDAMENTO -> "status-andamento";
                case CONCLUIDO    -> "status-concluido";
                case CANCELADO    -> "status-cancelado";
            };
        }
    }
}