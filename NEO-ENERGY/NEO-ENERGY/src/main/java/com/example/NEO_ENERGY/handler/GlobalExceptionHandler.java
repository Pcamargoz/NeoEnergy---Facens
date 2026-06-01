package com.example.NEO_ENERGY.handler;

import com.example.NEO_ENERGY.exception.OperacaoNaoPermitidaException;
import com.example.NEO_ENERGY.exception.RecursoNaoEncontradoException;
import com.example.NEO_ENERGY.exception.RegistroDuplicado;
import com.example.NEO_ENERGY.objects.dto.ErroCampoDTO;
import com.example.NEO_ENERGY.objects.dto.ErroRespostaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RegistroDuplicado.class)
    public ResponseEntity<ErroRespostaDTO> handleRegistroDuplicado(RegistroDuplicado ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroRespostaDTO.conflito(ex.getMessage()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroRespostaDTO> handleRecursoNaoEncontradoException(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroRespostaDTO(HttpStatus.NOT_FOUND.value(), ex.getMessage(), List.of()));
    }

    @ExceptionHandler(OperacaoNaoPermitidaException.class)
    public ResponseEntity<ErroRespostaDTO> handleOperacaoNaoPermitidaException(OperacaoNaoPermitidaException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErroRespostaDTO(HttpStatus.FORBIDDEN.value(), ex.getMessage(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroRespostaDTO> handleValidation(MethodArgumentNotValidException ex) {
        List<ErroCampoDTO> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErroCampoDTO(fe.getDefaultMessage(), fe.getField()))
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroRespostaDTO(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Erro de validação", erros));
    }

    /**
     * Handler genérico: captura qualquer exceção não tratada, loga stacktrace completo
     * e devolve mensagem detalhada no body (temporário, para diagnóstico em produção).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroRespostaDTO> handleGenerico(Exception ex) {
        log.error("[GlobalExceptionHandler] Exceção não tratada:", ex);
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        StringBuilder detalhe = new StringBuilder();
        detalhe.append(ex.getClass().getSimpleName());
        if (ex.getMessage() != null) {
            detalhe.append(": ").append(ex.getMessage());
        }
        if (root != ex) {
            detalhe.append(" | causa: ").append(root.getClass().getSimpleName());
            if (root.getMessage() != null) {
                detalhe.append(": ").append(root.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroRespostaDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), detalhe.toString(), List.of()));
    }
}
