import json
import requests
from dreamspeaker.util import dream_to_json


class NotionDreamPoster:
    """
        Posts formatted dreams to a Notion database.
    """

    NOTION_URL = "https://api.notion.com/v1/pages"

    def __init__(self, notion_token, database_id):
        self.database_id = database_id
        self.headers = {
            "Authorization": f"Bearer {notion_token}",
            "Content-Type": "application/json",
            "Notion-Version": "2022-06-28",
        }

    def post_dream(self, title, claude_text, original_text, emoji, log=None):
        """
            Post a dream entry to Notion.

            Args:
                title (str): Dream title from Claude.
                claude_text (str): Cleaned dream text from Claude.
                original_text (str): Original transcription.
                emoji (str): Emoji for the dream.
                log (callable, optional): Logging function. Defaults to print.
        """
        if log is None:
            log = print

        data = dream_to_json(
            dream_title=title,
            dream_claude=claude_text,
            dream_original=original_text,
            emoji=emoji,
            database_id=self.database_id,
        )

        response = requests.post(
            self.NOTION_URL,
            headers=self.headers,
            data=json.dumps(data),
        )

        if response.status_code == 200:
            log("Successfully added dream to Notion!")
        else:
            log(f"Failed to add dream. Status code: {response.status_code}")
            log(response.text)
