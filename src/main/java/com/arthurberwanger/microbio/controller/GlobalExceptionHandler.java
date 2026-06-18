package com.arthurberwanger.microbio.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleBeanValidation(MethodArgumentNotValidException ex,
                                       RedirectAttributes ra,
                                       HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        ra.addFlashAttribute("erro", msg.isBlank() ? "Dados inválidos." : msg);
        return "redirect:" + referer(req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleConstraintViolation(ConstraintViolationException ex,
                                            RedirectAttributes ra,
                                            HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining("; "));
        ra.addFlashAttribute("erro", msg.isBlank() ? "Dados inválidos." : msg);
        return "redirect:" + referer(req);
    }

    /** Entidade não encontrada (orçamento/pedido/análise inexistente) → mensagem amigável, sem 500. */
    @ExceptionHandler(EntityNotFoundException.class)
    public String handleNotFound(EntityNotFoundException ex,
                                 RedirectAttributes ra,
                                 HttpServletRequest req) {
        ra.addFlashAttribute("erro", ex.getMessage() != null ? ex.getMessage() : "Registro não encontrado.");
        return "redirect:" + referer(req);
    }

    /** Regras de negócio violadas (transição inválida, análise inativa, etc.). */
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public String handleRegraNegocio(RuntimeException ex,
                                     RedirectAttributes ra,
                                     HttpServletRequest req) {
        ra.addFlashAttribute("erro", ex.getMessage() != null ? ex.getMessage() : "Operação não permitida.");
        return "redirect:" + referer(req);
    }

    private String referer(HttpServletRequest req) {
        String ref = req.getHeader("Referer");
        return ref != null && !ref.isBlank() ? ref : "/";
    }
}
