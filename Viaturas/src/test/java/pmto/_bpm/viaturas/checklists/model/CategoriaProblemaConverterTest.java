package pmto._bpm.viaturas.checklists.model;

import org.junit.jupiter.api.Test;
import pmto._bpm.viaturas.checklists.dto.CategoriaProblema;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoriaProblemaConverterTest {

    private final CategoriaProblemaConverter converter = new CategoriaProblemaConverter();

    @Test
    void convertToEntityAttributeShouldMapNewCategories() {
        assertEquals(CategoriaProblema.EQUIPAMENTOS_OPERACIONAIS,
                converter.convertToEntityAttribute("EQUIPAMENTOS_OPERACIONAIS"));
        assertEquals(CategoriaProblema.CLIMATIZACAO,
                converter.convertToEntityAttribute("CLIMATIZACAO"));
        assertEquals(CategoriaProblema.PAINEL_COMANDOS,
                converter.convertToEntityAttribute("PAINEL_COMANDOS"));
        assertEquals(CategoriaProblema.LIMPADORES_VISIBILIDADE,
                converter.convertToEntityAttribute("LIMPADORES_VISIBILIDADE"));
        assertEquals(CategoriaProblema.INTERIOR_CABINE,
                converter.convertToEntityAttribute("INTERIOR_CABINE"));
        assertEquals(CategoriaProblema.CARROCERIA_EXTERNOS,
                converter.convertToEntityAttribute("CARROCERIA_EXTERNOS"));
        assertEquals(CategoriaProblema.VIDROS_RETROVISORES,
                converter.convertToEntityAttribute("VIDROS_RETROVISORES"));
        assertEquals(CategoriaProblema.MECANICA_GERAL,
                converter.convertToEntityAttribute("MECANICA_GERAL"));
        assertEquals(CategoriaProblema.FAROL,
                converter.convertToEntityAttribute("FAROL"));
    }

    @Test
    void convertToEntityAttributeShouldFallbackToGeralForUnknownCategory() {
        assertEquals(CategoriaProblema.GERAL,
                converter.convertToEntityAttribute("CATEGORIA_DESCONHECIDA"));
    }
}
