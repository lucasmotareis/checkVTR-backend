package pmto._bpm.viaturas.checklists.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pmto._bpm.viaturas.checklists.dto.CategoriaProblema;

import java.text.Normalizer;
import java.util.Locale;

@Converter
public class CategoriaProblemaConverter implements AttributeConverter<CategoriaProblema, String> {

    @Override
    public String convertToDatabaseColumn(CategoriaProblema attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public CategoriaProblema convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String normalized = normalize(dbData);
        return switch (normalized) {
            case "FAROL", "FAROIS" -> CategoriaProblema.FAROL;
            case "LUZ", "LUZES" -> CategoriaProblema.LUZES;
            case "PNEU", "PNEUS" -> CategoriaProblema.PNEUS;
            case "SINALIZACAO_POLICIAL" -> CategoriaProblema.SINALIZACAO_POLICIAL;
            case "ITENS_OBRIGATORIOS" -> CategoriaProblema.ITENS_OBRIGATORIOS;
            case "EQUIPAMENTOS_OPERACIONAIS" -> CategoriaProblema.EQUIPAMENTOS_OPERACIONAIS;
            case "CLIMATIZACAO" -> CategoriaProblema.CLIMATIZACAO;
            case "PAINEL_COMANDOS" -> CategoriaProblema.PAINEL_COMANDOS;
            case "LIMPADORES_VISIBILIDADE" -> CategoriaProblema.LIMPADORES_VISIBILIDADE;
            case "INTERIOR_CABINE" -> CategoriaProblema.INTERIOR_CABINE;
            case "CARROCERIA_EXTERNOS" -> CategoriaProblema.CARROCERIA_EXTERNOS;
            case "VIDROS_RETROVISORES" -> CategoriaProblema.VIDROS_RETROVISORES;
            case "MECANICA_GERAL", "MECANICA" -> CategoriaProblema.MECANICA_GERAL;
            case "COMBUSTIVEL" -> CategoriaProblema.COMBUSTIVEL;
            case "REFRIGERACAO" -> CategoriaProblema.REFRIGERACAO;
            case "MOTOR" -> CategoriaProblema.MOTOR;
            case "ELETRICA" -> CategoriaProblema.ELETRICA;
            case "BATERIA" -> CategoriaProblema.BATERIA;
            case "GERAL" -> CategoriaProblema.GERAL;
            default -> CategoriaProblema.GERAL;
        };
    }

    private String normalize(String value) {
        String upper = value.trim().toUpperCase(Locale.ROOT);
        String withoutAccents = Normalizer.normalize(upper, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        return withoutAccents
                .replace(' ', '_')
                .replace('-', '_');
    }
}
