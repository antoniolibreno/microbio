package com.arthurberwanger.microbio.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "pedido_analise")
@Data
@NoArgsConstructor
public class PedidoAnalise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analise_id", nullable = false)
    private Analise analise;

    @Column(columnDefinition = "TEXT")
    private String resultado;

    @Column(name = "valor_referencia", length = 200)
    private String valorReferencia;

    @Column(length = 20)
    private String conformidade = "PENDENTE";

    @Column(name = "data_realizacao")
    private LocalDate dataRealizacao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}