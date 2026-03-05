# DreamSpeaker

A backend that records dream audio, transcribes it with Whisper, cleans it up with Claude, and posts it to a Notion database.

## Setup

### Prerequisites

- Python 3.12+
- [uv](https://docs.astral.sh/uv/) package manager

### Install dependencies

```bash
uv sync
```

### Configure keys

Copy the example env file and fill in your values:

```bash
cp keys.env.example keys.env
```

The file expects four keys:

| Key | Description |
|---|---|
| `CLAUDE` | Your Anthropic API key (starts with `sk-ant-...`). Get one at https://console.anthropic.com/ |
| `DATABASE` | Your Notion database ID. This is the 32-character hex string in your Notion database URL — e.g. in `https://www.notion.so/myworkspace/abc123def456...?v=...`, the ID is `abc123def456...` (the part between the last `/` and the `?`). |
| `NOTION` | Your Notion integration token (starts with `secret_...`). Create an internal integration at https://www.notion.so/profile/integrations, then share your database with it. |
| `API_KEY` | A password you choose yourself. The app will ask for it so that it can connect to the server from unauthorized access. Pick any string you want. |

### Run the server

```bash
uv run dreamspeaker
```

You can specify a custom port with `--port`:

```bash
uv run dreamspeaker --port 31415
```

The server starts on `http://0.0.0.0:5000` if port not specified. 

In the app, you specify the server URL and API key. The app sends audio files at the specified URL to `POST /upload`, so make sure that if you use a reverse proxy, it forwards requests to that endpoint correctly.
