package com.example.whatsapp.service;

import com.example.whatsapp.config.WhatsappProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Servicio responsable de:
 * 1. Obtener la URL de descarga de un audio recibido por WhatsApp (usando el media ID).
 * 2. Descargar el archivo de audio binario.
 * 3. Enviarlo a la API de Whisper (OpenAI) para transcribir el audio a texto.
 */
@Service
public class AudioTranscriptionService {

    private static final Logger log = LoggerFactory.getLogger(AudioTranscriptionService.class);

    private static final String GRAPH_BASE_URL = "https://graph.facebook.com";
    private static final String WHISPER_URL    = "https://api.openai.com/v1/audio/transcriptions";
    private static final String WHISPER_MODEL  = "whisper-1";

    private final WhatsappProperties properties;
    private final String openAiApiKey;

    public AudioTranscriptionService(WhatsappProperties properties,
                                     @Value("${openai.api-key}") String openAiApiKey) {
        this.properties   = properties;
        this.openAiApiKey = openAiApiKey;
    }

    /**
     * Dado el mediaId de un audio de WhatsApp, descarga el audio y devuelve su transcripción.
     *
     * @param mediaId El ID del objeto de media retornado por el webhook de WhatsApp
     * @return El texto transcripto del audio, o null si ocurre un error
     */
    public String transcribe(String mediaId) {
        try {
            // Paso 1: Obtener la URL de descarga del media
            String mediaUrl = fetchMediaUrl(mediaId);
            log.info("[Audio] URL de descarga obtenida para mediaId={}: {}", mediaId, mediaUrl);

            // Paso 2: Descargar el contenido binario del audio
            byte[] audioBytes = downloadAudio(mediaUrl);
            log.info("[Audio] Audio descargado exitosamente, tamaño={} bytes", audioBytes.length);

            // Paso 3: Transcribir con Whisper
            String transcript = callWhisper(audioBytes, mediaId);
            log.info("[Audio] Transcripción completada para mediaId={}: {}", mediaId, transcript);
            return transcript;

        } catch (Exception e) {
            log.error("[Audio] Error al transcribir audio mediaId={}: {}", mediaId, e.getMessage(), e);
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Pasos internos
    // -------------------------------------------------------------------------

    /** Consulta la Graph API para obtener la URL temporal de descarga del media */
    private String fetchMediaUrl(String mediaId) {
        String url = GRAPH_BASE_URL + "/" + properties.apiVersion() + "/" + mediaId;
        RestClient graphClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + properties.accessToken())
                .build();

        MediaMetadata metadata = graphClient.get()
                .uri(url)
                .retrieve()
                .body(MediaMetadata.class);

        if (metadata == null || metadata.url() == null) {
            throw new IllegalStateException("No se pudo obtener la URL del media para id=" + mediaId);
        }
        return metadata.url();
    }

    /** Descarga el archivo de audio binario desde la URL temporal de Meta */
    private byte[] downloadAudio(String mediaUrl) {
        RestClient downloadClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + properties.accessToken())
                .build();

        return downloadClient.get()
                .uri(mediaUrl)
                .retrieve()
                .body(byte[].class);
    }

    /** Envía el audio a la API de Whisper y devuelve el texto transcripto */
    private String callWhisper(byte[] audioBytes, String mediaId) {
        // El webhook de WhatsApp generalmente envía audios en formato .ogg (Opus)
        // Whisper acepta: mp3, mp4, mpeg, mpga, m4a, wav, webm, ogg
        ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                // El nombre de archivo con extensión es requerido por la API multipart de Whisper
                return mediaId + ".ogg";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", audioResource);
        body.add("model", WHISPER_MODEL);
        body.add("language", "es"); // Indicamos español para mejorar precisión

        RestClient whisperClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + openAiApiKey)
                .build();

        WhisperResponse response = whisperClient.post()
                .uri(WHISPER_URL)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(WhisperResponse.class);

        if (response == null || response.text() == null || response.text().isBlank()) {
            throw new IllegalStateException("Whisper devolvió una transcripción vacía");
        }
        return response.text();
    }

    // -------------------------------------------------------------------------
    // DTOs internos
    // -------------------------------------------------------------------------

    /** Respuesta de la Graph API al consultar un media ID */
    private record MediaMetadata(
            String url,
            @JsonProperty("mime_type") String mimeType,
            @JsonProperty("file_size") Long fileSize,
            String id
    ) {}

    /** Respuesta de la API de Whisper */
    private record WhisperResponse(String text) {}
}
