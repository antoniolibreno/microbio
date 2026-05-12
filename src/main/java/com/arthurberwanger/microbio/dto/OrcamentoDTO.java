package com.arthurberwanger.microbio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrcamentoDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150)
    private String nome;

    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String telefone;

    @NotBlank(message = "Tipo de serviço é obrigatório")
    @Size(max = 100)
    private String tipoServico;

    // getters e setters
    public String getNome()                { return nome; }
    public void   setNome(String nome)     { this.nome = nome; }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }

    public String getTelefone()                    { return telefone; }
    public void   setTelefone(String telefone)     { this.telefone = telefone; }

    public String getTipoServico()                  { return tipoServico; }
    public void   setTipoServico(String tipoServico){ this.tipoServico = tipoServico; }
}