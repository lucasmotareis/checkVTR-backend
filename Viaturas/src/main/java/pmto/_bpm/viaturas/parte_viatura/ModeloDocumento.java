package pmto._bpm.viaturas.parte_viatura;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.Pictures;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModeloDocumento {

    public static void main(String[] args) throws Exception {

        Map<String, Object> data = new HashMap<>();
        data.put("data", "25/12/2025");
        data.put("hora", "14:35");
        data.put("endereco", "Av. Teotônio Segurado, Quadra 302 Norte");
        data.put("setor", "Plano Diretor Norte");
        data.put("cidade", "Palmas");
        data.put("uf", "TO");

// Viatura
        data.put("prefixo", "VTR-1234");
        data.put("modelo_veiculo", "Toyota Hilux SW4");
        data.put("placa", "QWE-9A87");
        data.put("chassi", "9BWZZZ377VT004251");

// Motorista
        data.put("nome_motorista", "Lucas Mota Reis");
        data.put("telefone", "(63) 99999-1234");
        data.put("graduacao", "3º Sargento");
        data.put("cpf", "123.456.789-00");
        data.put("rg", "1.234.567 SSP/TO");
        data.put("num_cnh", "01234567890");
        data.put("data_prim_cnh", "15/08/2010");
        data.put("data_venc_cnh", "15/08/2030");
        data.put("categoria", "AB");

// Terceiro envolvido
        data.put("nome_terceiro", "João da Silva");
        data.put("telefone_terceiro", "(63) 98888-4321");
        data.put("endereco_terceiro", "Rua das Acácias, nº 45");
        data.put("bairro_terceiro", "Jardim Aureny III");
        data.put("município_terceiro", "Palmas");
        data.put("cep_terceiro", "77000-000");
        data.put("estado_terceiro", "TO");
        data.put("veiculo_terceiro", "Honda Civic");
        data.put("placa_terceiro", "ABC-1D23");

// Vítima / observações do terceiro
        data.put("telefone_vitima", "(63) 97777-0000");
        data.put("lesao_vitima", "Escoriações leves");
        data.put("hospital_vitima", "Hospital Geral de Palmas");
        data.put("observacao_terceiro", "Condutor do veículo terceiro alegou não ter visto a viatura.");

// Fatos do acidente
        data.put(
                "fatos_acidente",
                "envolveu-se em colisão lateral com veículo terceiro ao realizar conversão à esquerda"
        );

// Condições da via
        data.put("boolean_sinalizacao", "SIM");
        data.put("sinalizacao", "Faixa contínua e sinalização vertical visível");
        data.put("tipo_via", "Asfalto em bom estado, via plana");
        data.put("velocidade_permitida", "60 km/h");
        data.put("velocidade_veiculo", "40 km/h");

// Observações finais
        data.put(
                "observacoes_local",
                "Clima seco, boa visibilidade. Não houve necessidade de interdição da via."
        );

        List<PictureRenderData> fotosViatura = new ArrayList<>();

        fotosViatura.add(
                Pictures.ofUrl(
                        "https://fotos-viatura-pmto.s3.us-east-2.amazonaws.com/checklists/2025-11-18/8%20BPM/PM-0866/RMB0B20/11785306/e9b1d253-41d9-40bf-b79f-b37da3be78ef.jpg"
                ).size(500, 400).create()
        );

        fotosViatura.add(
                Pictures.ofUrl(
                        "https://fotos-viatura-pmto.s3.us-east-2.amazonaws.com/checklists/2025-11-18/8%20BPM/PM-0866/RMB0B20/11785306/e9b1d253-41d9-40bf-b79f-b37da3be78ef.jpg"
                ).size(500, 400).create()
        );

        fotosViatura.add(
                Pictures.ofUrl(
                        "https://fotos-viatura-pmto.s3.us-east-2.amazonaws.com/checklists/2025-11-18/8%20BPM/PM-0866/RMB0B20/11785306/e9b1d253-41d9-40bf-b79f-b37da3be78ef.jpg"
                ).size(500, 400).create()
        );

        data.put("fotosViatura", fotosViatura);








        InputStream templateStream =
                ModeloDocumento.class
                        .getClassLoader()
                        .getResourceAsStream("templates/template.docx");

        if (templateStream == null) {
            throw new RuntimeException("template.docx não encontrado no classpath");
        }

        XWPFTemplate template = XWPFTemplate
                .compile(templateStream)
                .render(data);

        Path outputPath = Paths.get(
                System.getProperty("user.home"),
                "Desktop",
                "output.docx"
        );

        template.writeAndClose(
                new FileOutputStream(outputPath.toFile())
        );

        System.out.println("Arquivo gerado em: " + outputPath);
    }
}
