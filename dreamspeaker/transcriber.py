import threading
from faster_whisper import WhisperModel


class DreamTranscriber:
    """
        Speech-to-text wrapper using faster-whisper.
        Model is loaded lazily on first transcription and unloaded
        after a period of inactivity to free RAM.
    """

    def __init__(self, model_size="large-v3", compute_type="float32", unload_after=300):
        """
            Args:
                model_size: Whisper model size.
                compute_type: Compute type (int8, int8_float32, float32).
                unload_after: Seconds of inactivity before unloading the model (default 300 = 5 min).
        """
        self.model_size = model_size
        self.compute_type = compute_type
        self.unload_after = unload_after

        self._model = None
        self._lock = threading.Lock()
        self._unload_timer = None

    def _load_model(self):
        if self._model is None:
            self._model = WhisperModel(self.model_size, device="cpu", compute_type=self.compute_type)

    def _reset_timer(self):
        if self._unload_timer is not None:
            self._unload_timer.cancel()
        self._unload_timer = threading.Timer(self.unload_after, self._unload_model)
        self._unload_timer.daemon = True
        self._unload_timer.start()

    def _unload_model(self):
        with self._lock:
            self._model = None
            self._unload_timer = None

    def transcribe(self, audio_path, log=None):
        """
            Transcribe an audio file to text.

            Args:
                audio_path (str): Path to the audio file.
                log (callable, optional): Logging function.

            Returns:
                str: The full transcription text.
        """
        if log is None:
            log = print

        with self._lock:
            if self._model is None:
                log("Loading Whisper model...")
                self._load_model()
                log("Whisper model loaded!")

            segments, _ = self._model.transcribe(
                audio_path,
                vad_filter=True,
            )
            result = " ".join(segment.text.strip() for segment in segments)

        self._reset_timer()
        return result
