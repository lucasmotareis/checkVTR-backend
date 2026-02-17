package pmto._bpm.viaturas.analytics.dto;

import java.time.Instant;

public record Periodo(Instant inicio, Instant fim) {

    public static Periodo of(Instant inicio, Instant fim) {
        Instant inicioEfetivo = (inicio != null) ? inicio : Instant.EPOCH;
        Instant fimEfetivo = (fim != null) ? fim : Instant.now();
        return new Periodo(inicioEfetivo, fimEfetivo);
    }
}
