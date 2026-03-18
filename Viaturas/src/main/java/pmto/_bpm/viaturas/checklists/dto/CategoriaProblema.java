package pmto._bpm.viaturas.checklists.dto;

public enum CategoriaProblema {
    FAROL(1, true),
    LUZES(1, true),
    PNEUS(2, true),
    SINALIZACAO_POLICIAL(3, true),
    ITENS_OBRIGATORIOS(4, true),
    EQUIPAMENTOS_OPERACIONAIS(5, false),
    CLIMATIZACAO(6, false),
    PAINEL_COMANDOS(7, false),
    LIMPADORES_VISIBILIDADE(8, false),
    INTERIOR_CABINE(9, false),
    CARROCERIA_EXTERNOS(10, false),
    VIDROS_RETROVISORES(11, false),
    MECANICA_GERAL(12, false),
    COMBUSTIVEL(13, false),
    REFRIGERACAO(14, false),
    MOTOR(15, false),
    ELETRICA(16, false),
    BATERIA(17, false),
    GERAL(99, false);

    private final int ordemExibicao;
    private final boolean monitorarNaViatura;

    CategoriaProblema(int ordemExibicao, boolean monitorarNaViatura) {
        this.ordemExibicao = ordemExibicao;
        this.monitorarNaViatura = monitorarNaViatura;
    }

    public int getOrdemExibicao() {
        return ordemExibicao;
    }

    public boolean isMonitorarNaViatura() {
        return monitorarNaViatura;
    }
}
