# Driver Registration API

Base path: `/api/driver`

All responses (success and error) are wrapped in the same envelope:

```json
{
  "data": { },
  "message": "Human readable message",
  "status": 200
}
```

Validation errors (`400`, from `@Valid` failures) return `data` as a map of `{ "fieldName": "error message" }` instead of an object, with `message: "Validation failed"`.

## Auth model

- `POST /register` is **public** (no token required).
- All other endpoints in this controller require a valid JWT.
- The JWT is delivered as **httpOnly cookies** (`accessToken`, `refreshToken`), not as a bearer token in the response body — `register` sets these cookies automatically on success, effectively logging the new driver in. The frontend does not need to call `/api/auth/login` separately after registration, but does need to send credentials (`credentials: 'include'` / cookie jar) on subsequent requests.
- Cookie flags: `HttpOnly`, `SameSite=Strict`, `Secure=false` (dev config), `Path=/`. Max-age comes from `app.jwt.expiration` / `app.jwt.refresh-expiration`.

---

## 1. Register a driver

`POST /api/driver/register`

Public. Creates a `User` (role `DRIVER`) and a `DriverApplication` in `DRAFT` status, then logs the user in (sets auth cookies).

### Request body — `application/json`

| Field | Type | Required | Notes |
|---|---|---|---|
| `firstName` | string | yes | |
| `lastName` | string | yes | |
| `fullName` | string | yes | |
| `dateOfBirth` | string (`yyyy-MM-dd`) | yes | must be in the past |
| `phone` | string | yes | |
| `whatsapp` | string | no | |
| `nicNumber` | string | yes | must be unique; stored uppercased |
| `languagesSpoken` | string[] | yes | at least one |
| `yearsOfExperience` | integer | yes | ≥ 0 |
| `availability` | enum | yes | one of `FULL_TIME`, `PART_TIME`, `TRANSFERS_ONLY`, `SEASONAL` |
| `email` | string | yes | must be unique, valid email format |
| `password` | string | yes | min 8 chars |
| `privacyPolicyAccepted` | boolean | yes | must be `true` |
| `privacyPolicyVersion` | string | no | defaults to `"1.0"` |

```json
{
  "firstName": "Kasun",
  "lastName": "Perera",
  "fullName": "Kasun Perera",
  "dateOfBirth": "1990-05-12",
  "phone": "+94771234567",
  "whatsapp": "+94771234567",
  "nicNumber": "902345678V",
  "languagesSpoken": ["English", "Sinhala"],
  "yearsOfExperience": 5,
  "availability": "FULL_TIME",
  "email": "kasun@example.com",
  "password": "SecurePass123",
  "privacyPolicyAccepted": true,
  "privacyPolicyVersion": "1.0"
}
```

### Success — `201 Created`

```json
{
  "data": {
    "id": 42,
    "applicationNumber": "DK-2026-0042",
    "status": "DRAFT",
    "fullName": "Kasun Perera",
    "email": "kasun@example.com",
    "privacyPolicyAccepted": true,
    "privacyPolicyVersion": "1.0",
    "submittedAt": null,
    "createdAt": "2026-09-19T10:15:30"
  },
  "message": "Registration submitted successfully",
  "status": 201
}
```
Auth cookies (`accessToken`, `refreshToken`) are set on this response — the driver is now authenticated.

Use `data.id` as `applicationId` for the document upload step below.

### Errors

| Status | When |
|---|---|
| 400 | Validation failed (see field table above) — `data` is a field→message map |
| 409 | Email already registered, or NIC number already used for another application |

---

## 2. Upload application documents

`POST /api/driver/register/{applicationId}/documents`

Requires auth (the driver just registered above, or logs back in). Only the driver who owns the application may upload to it. Application must currently be in `DRAFT` status — this can only be done once. On success the application moves to `PENDING`.

Content type: `multipart/form-data`

| Path param | Type |
|---|---|
| `applicationId` | number |

| Form field | Required | Notes |
|---|---|---|
| `licenceFront` | yes | file |
| `licenceBack` | yes | file |
| `policeClearance` | yes | file |

File constraints (apply to all three):
- Allowed types: **JPEG, PNG only** (`image/jpeg`, `image/png`) — no PDFs
- Max size: **10 MB** each
- Must not be empty

### Success — `200 OK`

```json
{
  "data": {
    "id": 42,
    "applicationNumber": "DK-2026-0042",
    "status": "PENDING",
    "fullName": "Kasun Perera",
    "email": "kasun@example.com",
    "privacyPolicyAccepted": true,
    "privacyPolicyVersion": "1.0",
    "submittedAt": "2026-09-19T10:20:00",
    "createdAt": "2026-09-19T10:15:30"
  },
  "message": "Documents uploaded successfully. Application is now under review.",
  "status": 200
}
```

### Errors

| Status | When |
|---|---|
| 400 | A file is empty, exceeds 10MB, or has an unsupported content type; or the application is not in `DRAFT` (documents already submitted) |
| 401 | Not authenticated |
| 403 | Authenticated user does not own this application |
| 404 | Application not found |

---

## 3. Get my application

`GET /api/driver/application/me`

Requires auth. Returns the `DriverApplication` belonging to the currently authenticated user. Use this to check application status (e.g. poll for `APPROVED`/`REJECTED`) or resume an incomplete registration.

### Success — `200 OK`

Same `DriverApplicationResponseDto` shape as above. Possible `status` values: `DRAFT`, `PENDING`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`.

### Errors

| Status | When |
|---|---|
| 401 | Not authenticated |
| 404 | No application exists for this account |

---

## 4. Update password

`PUT /api/driver/password`

Requires auth. Not driver-specific despite the path — delegates to the shared account service.

### Request body — `application/json`

| Field | Type | Required | Notes |
|---|---|---|---|
| `currentPassword` | string | yes | |
| `newPassword` | string | yes | min 8 chars, must differ from current |
| `confirmPassword` | string | yes | must match `newPassword` |

```json
{
  "currentPassword": "SecurePass123",
  "newPassword": "EvenMoreSecure456",
  "confirmPassword": "EvenMoreSecure456"
}
```

### Success — `200 OK`

```json
{
  "data": null,
  "message": "Password updated successfully. Please log in again.",
  "status": 200
}
```

Note: the server invalidates the existing session/refresh token on password change, so the frontend should redirect to login (or re-authenticate) after this call — existing cookies will no longer work for refresh.

### Errors

| Status | When |
|---|---|
| 400 | Validation failed; `newPassword` != `confirmPassword`; current password incorrect; new password same as current |
| 401 | Not authenticated |
| 404 | User not found |

---

## Suggested frontend flow

1. Driver fills registration form → `POST /register` → store returned `applicationId`, cookies now set.
2. Immediately (or in a follow-up step) prompt for the 3 document uploads → `POST /register/{applicationId}/documents`.
3. On the driver dashboard/status page, call `GET /application/me` to show current status (`DRAFT` → still needs documents; `PENDING`/`UNDER_REVIEW` → awaiting admin review; `APPROVED`/`REJECTED` → final outcome).
4. Account settings page uses `PUT /password` for password changes, then forces re-login.
