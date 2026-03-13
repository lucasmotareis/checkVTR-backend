package pmto._bpm.viaturas.checklists.dto;

public enum CategoriaProblema {
    LUZES(1, true),
    PNEUS(2, true),
    SINALIZACAO_POLICIAL(3, true),
    ITENS_OBRIGATORIOS(4, true),
    COMBUSTIVEL(5, false),
    REFRIGERACAO(6, false),
    MOTOR(7, false),
    ELETRICA(8, false),
    BATERIA(9, false),
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