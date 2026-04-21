# DocBot API

A Spring Boot backend that lets users upload documents and chat with them using Google Gemini via the Google ADK (Agent Development Kit).

## How It Works

1. **Upload** a document (PDF, DOCX, etc.) — text is extracted in memory using Apache Tika
2. **Chat** with the document — extracted text is sent to Google Gemini, which answers your questions

## Tech Stack

- Java 17, Spring Boot 3.3.5
- Google ADK 0.1.0 (Gemini 2.5 Flash)
- Apache Tika (document text extraction)
- SpringDoc OpenAPI (Swagger UI)
- Lombok

## Prerequisites

- Java 17+
- Maven
- `GOOGLE_API_KEY` environment variable set with a valid Gemini API key (starts with `AIza`)

## Run

```bash
mvn spring-boot:run
```

Server starts at `http://localhost:8080`

## API Endpoints

### Files

| Method | URL                          | Description              |
|--------|------------------------------|--------------------------|
| POST   | `/api/files/upload`          | Upload a file            |
| GET    | `/api/files`                 | List all files           |
| GET    | `/api/files/{fileId}`        | Get file by ID           |
| GET    | `/api/files/user/{userId}`   | List files by user       |
| DELETE | `/api/files/{fileId}`        | Delete a file            |

### Chat

| Method | URL                          | Description              |
|--------|------------------------------|--------------------------|
| POST   | `/api/chat`                  | Chat with a document     |
| GET    | `/api/chat/history/{fileId}` | Get chat history         |

## Swagger / OpenAPI

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Quick Test (Postman)

**1. Upload a file:**

```
POST http://localhost:8080/api/files/upload
Content-Type: multipart/form-data

file: <your-file.pdf>
userId: user1
```

**2. Chat with it:**

```
POST http://localhost:8080/api/chat
Content-Type: application/json

{
  "fileId": "<id-from-upload-response>",
  "message": "What is this document about?"
}
```
