package harvestLog.controller;

import harvestLog.model.Farmer;
import harvestLog.model.PlanType;
import harvestLog.service.PlanService;
import harvestLog.service.impl.FarmerService;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static harvestLog.security.FarmerIdExtractor.getAuthenticatedFarmerId;

@RestController
@RequestMapping("/api/ai")
public class TranscriptionController {

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final FarmerService farmerService;
    private final PlanService planService;

    public TranscriptionController(OpenAiAudioTranscriptionModel transcriptionModel,
                                   FarmerService farmerService,
                                   PlanService planService) {
        this.transcriptionModel = transcriptionModel;
        this.farmerService = farmerService;
        this.planService = planService;
    }

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribe(@RequestParam("audio") MultipartFile audio) throws Exception {
        Farmer farmer = farmerService.findById(getAuthenticatedFarmerId());
        if (planService.getEffectivePlan(farmer) == PlanType.FREE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"code\":\"PREMIUM_REQUIRED\",\"message\":\"AI features require FARM plan.\"}");
        }

        ByteArrayResource audioResource = new ByteArrayResource(audio.getBytes()) {
            @Override
            public String getFilename() { return "recording.webm"; }
        };

        var options = OpenAiAudioTranscriptionOptions.builder()
                .model("whisper-1")
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .build();

        String transcript = transcriptionModel
                .call(new AudioTranscriptionPrompt(audioResource, options))
                .getResult()
                .getOutput();

        return ResponseEntity.ok(transcript);
    }
}
