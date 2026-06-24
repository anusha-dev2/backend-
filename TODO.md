# TODO - Split Screen POST API

- [ ] Inspect existing split-screen GET payload logic in ContentController (`/content/screen/{deviceId}`)
- [ ] Add new POST endpoint: `/api/content/screen/layoutMode` in ContentController
- [ ] Create request DTO inside controller (or separate DTO) with fields:
  - deviceId (required)
  - layoutMode (optional; supports HORIZONTAL/VERTICAL)
- [ ] Build response with zones:
  - If layoutMode == HORIZONTAL: CONTENT_1 = left, CONTENT_2 = right
  - If layoutMode == VERTICAL: CONTENT_1 = top, CONTENT_2 = bottom
- [ ] Ensure server saves/updates device setting.layoutMode based on request (so GET uses same value)
- [ ] Add basic validation + error handling (device not found)
- [ ] Run build/test (compile)

