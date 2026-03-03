import anthropic
import json

class ClaudeDreamCorrection():
    """
        Wrapper for the Claude API, used to correct and title dreams.
    """

    def __init__(self, api_key):
        self.client = anthropic.Anthropic(api_key=api_key)
        
        self.system = "You will be given a dream journal entry that was transcribed from a voice recording. It may contain speech-to-text artifacts such as filler words, repetitions, false starts, or misrecognized words. Your task is to clean up these issues and create a short title for the dream. Here's what you need to do:\n\n1. Read the dream journal entry\n\n2. Clean up the text. The original entry was transcribed from a voice recording, so there may be speech-to-text errors, filler words (um, uh, euh, ben), repetitions, and false starts. Remove these while maintaining the original meaning and style of the text.\n\n3. Format the text nicely. This may include adding appropriate line breaks, paragraphs, or other formatting elements to improve readability.\n\n4. Preserve the language(s) used in the original text. The entry may be in French, English, or a mix of both. Do not translate any part of the text; keep it in its original language(s).\n\n5. Preserve the tone used in the original text. For example 'je sais pas' should NOT be corrected to 'je ne sais pas'. Similarly, 'I wanna go' should NOT be translated to 'I want to go'.\n\n6. Create a short, descriptive title for the dream based on its content. This title should be concise (no more than 5-7 words) and capture the essence or a key element of the dream.\n\n7. Choose an emoji that captures (somewhat) the subject of the dream. It is ok if it does not correspond exactly; but you should nonetheless always choose an emoji.\n\n8. Format your response as JSON with three keys:\n   - 'dream_title': containing the short title you created\n   - 'dream_text': containing the fixed and formatted dream text\n   - 'dream_emoji': containing a string with a single emoji\n\nYour output should look like this:\n\n{\n  \"dream_title\": \"Your short dream title here\",\n  \"dream_text\": \"The fixed and formatted dream text here\",\n  \"dream_emoji\": \"🌙\"\n}\n\nMake sure to include ONLY THE JSON FORMATTED DATA, and nothing else.\n\nRemember to maintain the original language(s) and overall style of the dream while cleaning up speech artifacts and improving formatting."
    
    def _request_dict(self, dream_text):
        return dict(
            model="claude-sonnet-4-5",
            max_tokens=2000,
            temperature=0,
            system=self.system,
            messages=[
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "text",
                            "text": dream_text
                        }
                    ]
                },
                {
                    "role": "assistant",
                    "content": "{"
                }
            ]
        )
    
    def correct_and_title(self, dream_text, log=None):
        """
            Given a dream text, corrects it using Claude and returns a JSON with the corrected text and a title.

            Args:
            dream_text (str): The dream text to correct.
            log (callable, optional): A logging function. If None, uses print.

            Returns:
            dict: Dictionary with keys 'dream_title', 'dream_text', and 'dream_emoji'.
        """
        if log is None:
            log = print

        response = self.client.messages.create(**self._request_dict(dream_text))
        raw = '{' + response.content[0].text
        log('---------- CLAUDE RAW RESPONSE ----------')
        log(raw)
        log('------------------------------------------')
        try:
            return json.loads(raw)
        except json.JSONDecodeError:
            log('Error in Claude response, not valid JSON.')
            return {'dream_title': 'Claude_Error', 'dream_text': response.content[0].text, 'dream_emoji': '🤖'}



