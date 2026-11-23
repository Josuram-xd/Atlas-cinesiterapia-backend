package co.edu.atlas.media;

import java.net.URI;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/** Cloudflare R2 por su API compatible con S3. Las credenciales llegan solo por variables de entorno. */
@Component
@ConditionalOnProperty(prefix = "app.media", name = "proveedor", havingValue = "r2")
public class AlmacenamientoR2 implements AlmacenamientoMedia {

    private final MediaProperties props;
    private final Clock clock;
    private final String bucket;
    private final S3Presigner presigner;
    private final S3Client cliente;

    public AlmacenamientoR2(MediaProperties props, Clock clock) {
        MediaProperties.R2 r2 = props.r2();
        exigir(r2.endpoint(), "R2_ENDPOINT");
        exigir(r2.accessKey(), "R2_ACCESS_KEY_ID");
        exigir(r2.secretKey(), "R2_SECRET_ACCESS_KEY");
        exigir(r2.bucket(), "R2_BUCKET");
        this.props = props;
        this.clock = clock;
        this.bucket = r2.bucket();
        var credenciales = StaticCredentialsProvider.create(AwsBasicCredentials.create(r2.accessKey(), r2.secretKey()));
        var region = Region.of(r2.region());
        var servicio = S3Configuration.builder().pathStyleAccessEnabled(true).build();
        this.presigner = S3Presigner.builder()
                .endpointOverride(URI.create(r2.endpoint()))
                .region(region)
                .credentialsProvider(credenciales)
                .serviceConfiguration(servicio)
                .build();
        this.cliente = S3Client.builder()
                .endpointOverride(URI.create(r2.endpoint()))
                .region(region)
                .credentialsProvider(credenciales)
                .serviceConfiguration(servicio)
                .httpClient(UrlConnectionHttpClient.create())
                .build();
    }

    private static void exigir(String valor, String variable) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno " + variable + " para el almacenamiento R2");
        }
    }

    @Override
    public SubidaPrefirmada prepararSubida(String clave, String contentType, long tamanoBytes) {
        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(clave)
                .contentType(contentType)
                .contentLength(tamanoBytes)
                .build();
        var firmada = presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(props.subidaTtl())
                .putObjectRequest(put)
                .build());
        return new SubidaPrefirmada(
                firmada.url().toString(), "PUT", Map.of("Content-Type", contentType),
                clock.instant().plus(props.subidaTtl()));
    }

    @Override
    public String urlLectura(String clave) {
        String base = props.publicBaseUrl();
        if (base != null && !base.isBlank()) {
            return base.replaceAll("/+$", "") + "/" + clave;
        }
        var firmada = presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(props.lecturaTtl())
                .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(clave).build())
                .build());
        return firmada.url().toString();
    }

    @Override
    public Optional<ObjetoAlmacenado> informacion(String clave) {
        try {
            var head = cliente.headObject(HeadObjectRequest.builder().bucket(bucket).key(clave).build());
            return Optional.of(new ObjetoAlmacenado(head.contentLength(), head.contentType()));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (software.amazon.awssdk.services.s3.model.S3Exception e) {
            if (e.statusCode() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Override
    public void eliminar(String clave) {
        cliente.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(clave).build());
    }
}
