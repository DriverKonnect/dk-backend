# Tour Company Registration & Approval API

Tour company sign-up is a **request** that an admin must approve. Registering does **not** log the company in and does **not** set any auth cookies. The company can only log in after an admin approves the registration.

All responses (success and error) are wrapped in the standard envelope:

```json
{
  "data": { },
  "message": "Human readable message",
  "status": 200
}
```

Validation errors (`400`) return `data` as a `{ "fieldName": "error message" }` map with `message: "Validation failed"`.

## Status lifecycle

`TourCompanyStatus`: `PENDING`, `APPROVED`, `REJECTED`, `DEACTIVATED`

| Action | Allowed from | Result | Can log in? |
|---|---|---|---|
| Register | (new) | `PENDING` | no |
| Resubmit (register again with same email/BRN) | `REJECTED` | `PENDING` | no |
| Approve | `PENDING`, `REJECTED` | `APPROVED` | yes |
| Reject (optional reason) | `PENDING` | `REJECTED` | no |
| Deactivate | `APPROVED` | `DEACTIVATED` (refresh tokens revoked) | no |
| Reactivate | `DEACTIVATED` | `APPROVED` | yes |

An action that is not allowed from the current status returns `400`.

---

## 1. Submit a registration request

`POST /api/tour-company/register` (public)

### Request body (`application/json`)

| Field | Type | Required | Notes |
|---|---|---|---|
| `companyName` | string | yes | |
| `businessRegistrationNumber` | string | yes | unique; stored uppercased |
| `contactPersonFirstName` | string | yes | |
| `contactPersonLastName` | string | yes | |
| `email` | string | yes | unique, valid email; stored lowercased |
| `phone` | string | yes | |
| `password` | string | yes | min 8 chars |

```json
{
  "companyName": "Island Tours (Pvt) Ltd",
  "businessRegistrationNumber": "PV12345",
  "contactPersonFirstName": "Nimal",
  "contactPersonLastName": "Silva",
  "email": "info@islandtours.example",
  "phone": "+94771234567",
  "password": "SecurePass123"
}
```

### Success: `201 Created`

```json
{
  "data": {
    "id": 7,
    "companyName": "Island Tours (Pvt) Ltd",
    "businessRegistrationNumber": "PV12345",
    "contactPersonFirstName": "Nimal",
    "contactPersonLastName": "Silva",
    "email": "info@islandtours.example",
    "phone": "+94771234567",
    "status": "PENDING",
    "createdAt": "2026-11-15T10:15:30"
  },
  "message": "Registration submitted. Awaiting admin approval.",
  "status": 201
}
```

If the email belongs to a `REJECTED` tour company, the same record is updated with the new details and password, and its status goes back to `PENDING` (it keeps the same `id`).

### Errors

| Status | When |
|---|---|
| 400 | Validation failed |
| 409 | Email already in use (by any non-rejected account), or BRN already used by another company |

---

## 2. Login behaviour for tour companies

`POST /api/auth/login` (unchanged endpoint)

If the email and password are correct but the company is not approved, login returns **403** with a specific message:

| Status | Message |
|---|---|
| `PENDING` | `Your registration is pending admin approval` |
| `REJECTED` | `Your registration was rejected` or `Your registration was rejected: <reason>` |
| `DEACTIVATED` | `Your account has been deactivated. Please contact support` |

A wrong password always returns the generic `401 Invalid email or password`, so the account's status is not revealed.

---

## 3. Admin endpoints

Base path: `/api/admin/tour-companies`. These require the `ADMIN` role, and non-admins get `403`.

### List: `GET /api/admin/tour-companies`

| Query param | Default | Notes |
|---|---|---|
| `status` | (all) | `PENDING`, `APPROVED`, `REJECTED`, `DEACTIVATED` |
| `page` | `0` | 0-indexed |
| `size` | `10` | |

Results are sorted newest first.

```json
{
  "data": {
    "content": [
      {
        "id": 7,
        "companyName": "Island Tours (Pvt) Ltd",
        "businessRegistrationNumber": "PV12345",
        "contactPersonFirstName": "Nimal",
        "contactPersonLastName": "Silva",
        "email": "info@islandtours.example",
        "phone": "+94771234567",
        "status": "PENDING",
        "isActive": false,
        "createdAt": "2026-11-15T10:15:30"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  },
  "message": "Tour companies retrieved successfully",
  "status": 200
}
```

### Get one: `GET /api/admin/tour-companies/{companyId}`

Returns the detail DTO, which has the summary fields plus `userId`, `rejectionReason`, `reviewedAt`, `reviewedByEmail` and `updatedAt`. Returns `404` if not found.

### Approve: `PUT /api/admin/tour-companies/{companyId}/approve`
No body. Allowed from `PENDING` or `REJECTED`.

### Reject: `PUT /api/admin/tour-companies/{companyId}/reject`
Optional body `{ "reason": "BRN could not be verified" }`. Allowed from `PENDING` only.

### Deactivate: `PUT /api/admin/tour-companies/{companyId}/deactivate`
No body. Allowed from `APPROVED` only. It disables the account and revokes refresh tokens. Note: an access token that was already issued stays valid until it expires (this is the same as for drivers).

### Reactivate: `PUT /api/admin/tour-companies/{companyId}/reactivate`
No body. Allowed from `DEACTIVATED` only.

All four actions return the detail DTO and use these errors: `400` invalid status transition, `404` company not found.

---

## Suggested frontend flow

1. The company fills in the sign-up form and calls `POST /api/tour-company/register`. Show a "Registration submitted, awaiting approval" screen. Do not redirect to the dashboard.
2. On the login page, show the `403` message from `/api/auth/login` as it is (pending, rejected with reason, or deactivated).
3. If rejected, the company can submit the form again with the same email to resubmit.
4. In the admin panel, list `?status=PENDING` and use the approve and reject actions. The full list (no filter) is used for deactivate and reactivate.
