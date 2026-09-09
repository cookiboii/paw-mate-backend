# Backend conventions

## Naming

- Request DTOs use `*Request`; response DTOs use `*Response`.
- A DTO name must state its role. Avoid ambiguous names such as `CommentDto` for a request body.
- Use plural resource names for new routes and the `/api/v1` prefix.

## API design

- Prefer resource-oriented endpoints: `POST /api/v1/animals`, `GET /api/v1/posts`, and `DELETE /api/v1/posts/{postId}`.
- Keep legacy routes only as explicit compatibility aliases and remove them in a versioned deprecation release.
- Request and response bodies must use DTOs, not `Map`.
- Success and error envelopes both include an application-level `code`.

## Controller and service boundaries

- Controllers validate and map HTTP concerns only; services own business rules and transactions.
- Put parameter validation on the API documentation interface when a controller implements it. Do not redefine validation constraints in the implementation.
- Obtain the authenticated member through one consistent mechanism per feature; do not silently fall back between principal types.

## Testing

- Unit tests must run without external infrastructure.
- Tests requiring Redis, a real database, or other services are tagged `integration` and run separately in CI.
- Every bug fix includes a regression test when the behavior is externally observable.
